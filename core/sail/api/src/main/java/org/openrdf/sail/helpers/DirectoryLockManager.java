/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessControlException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;

/**
 * Used to create a lock in a directory.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class DirectoryLockManager implements LockManager {

	private static final String LOCK_DIR_NAME = "lock";

	private static final String LOCK_FILE_NAME = "locked";

	private static final String INFO_FILE_NAME = "process";

	private final Logger logger = LoggerFactory.getLogger(DirectoryLockManager.class);

	private final Path dir;

	public DirectoryLockManager(Path dir) {
		this.dir = dir;
	}

	public String getLocation() {
		return dir.toString();
	}

	private Path getLockDir() {
		return dir.resolve(LOCK_DIR_NAME);
	}

	/**
	 * Determines if the directory is locked.
	 * 
	 * @return <code>true</code> if the directory is already locked.
	 */
	public boolean isLocked() {
		return Files.exists(getLockDir());
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock or null if the directory is already locked.
	 * @throws InterruptedException
	 */
	public Lock tryLock()
		throws InterruptedException
	{
		Path lockDir = getLockDir();

		if (Files.exists(lockDir)) {
			removeInvalidLock(lockDir);
		}

		Lock lock = null;

		try {
			Path directories = Files.createDirectories(lockDir);
			System.out.println("Lock directory: " + directories);
			if (directories == null) {
				return null;
			}

			Path infoFile = Files.createFile(lockDir.resolve(INFO_FILE_NAME));
			Path lockedFile = Files.createFile(lockDir.resolve(LOCK_FILE_NAME));

			System.out.println("Info file: " + infoFile);
			System.out.println("Lock file: " + lockedFile);
			// RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			AsynchronousFileChannel raf = AsynchronousFileChannel.open(lockedFile, StandardOpenOption.SYNC,
					StandardOpenOption.WRITE);
			try {
				FileLock fileLock = raf.lock().get();
				lock = createLock(raf, fileLock);
				sign(infoFile);
				System.out.println("Lock acquired");
			}
			catch (IOException | InterruptedException e) {
				if (lock != null) {
					// Also closes raf
					lock.release();
				}
				else {
					raf.close();
				}
				throw e;
			}
			catch (ExecutionException e) {
				raf.close();
			}
		}
		catch (IOException e) {
			logger.error(e.toString(), e);
			e.printStackTrace();
		}

		return lock;
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock.
	 * @throws SailLockedException
	 *         if the directory is already locked.
	 */
	public Lock lockOrFail()
		throws SailLockedException
	{
		String requestedBy = getProcessName();

		Lock lock;
		try {
			lock = tryLock();

			if (lock != null) {
				return lock;
			}

			String lockedBy = getLockedBy();

			if (lockedBy != null) {
				throw new SailLockedException(lockedBy, requestedBy, this);
			}

			lock = tryLock();
			if (lock != null) {
				return lock;
			}
		}
		catch (InterruptedException e) {
			throw new SailLockedException(requestedBy, e);
		}

		throw new SailLockedException(requestedBy);
	}

	/**
	 * Revokes a lock owned by another process.
	 * 
	 * @return <code>true</code> if a lock was successfully revoked.
	 */
	public boolean revokeLock() {
		Path lockDir = getLockDir();
		Path lockedFile = lockDir.resolve(LOCK_FILE_NAME);
		Path infoFile = lockDir.resolve(INFO_FILE_NAME);
		try {
			Files.delete(lockedFile);
			Files.delete(infoFile);
			Files.delete(lockDir);
			return true;
		}
		catch (IOException e) {
			logger.error("Error while revoking lock: ", e);
		}

		return false;
	}

	private void removeInvalidLock(Path lockDir) {
		try {
			boolean revokeLock = false;

			Path lockedFile = lockDir.resolve(LOCK_FILE_NAME);
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(lockedFile,
					StandardOpenOption.SYNC, StandardOpenOption.WRITE);
			try {
				FileLock fileLock = fileChannel.tryLock();

				if (fileLock != null) {
					logger.warn("Removing invalid lock {}", getLockedBy());
					fileLock.release();
					revokeLock = true;
				}
			}
			catch (OverlappingFileLockException exc) {
				// lock is still valid
			}
			finally {
				fileChannel.close();
			}

			if (revokeLock) {
				revokeLock();
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
		}
	}

	private String getLockedBy() {
		try {
			Path lockDir = getLockDir();
			Path infoFile = lockDir.resolve(INFO_FILE_NAME);
			BufferedReader reader = Files.newBufferedReader(infoFile, StandardCharsets.UTF_8);
			try {
				return reader.readLine();
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
			return null;
		}
	}

	private Lock createLock(final AsynchronousFileChannel raf, final FileLock fileLock) {
		return new Lock() {

			private Thread hook;
			{
				try {
					Thread hook = new Thread(new Runnable() {

						public void run() {
							delete();
						}
					});
					Runtime.getRuntime().addShutdownHook(hook);
					this.hook = hook;
				}
				catch (AccessControlException e) {
					// okay, just remember to close it yourself
				}
			}

			public boolean isActive() {
				return fileLock.isValid() || hook != null;
			}

			public void release() {
				try {
					if (hook != null) {
						Runtime.getRuntime().removeShutdownHook(hook);
						hook = null;
					}
				}
				catch (IllegalStateException e) {
					// already shutting down
				}
				catch (AccessControlException e) {
					logger.warn(e.toString(), e);
				}
				delete();
			}

			void delete() {
				try {
					if (raf.isOpen()) {
						fileLock.release();
						raf.close();
					}
				}
				catch (IOException e) {
					logger.warn(e.toString(), e);
				}

				revokeLock();
			}
		};
	}

	private void sign(Path infoFile)
		throws IOException
	{
		// FileWriter out = new FileWriter(infoFile);
		BufferedWriter out = Files.newBufferedWriter(infoFile, StandardCharsets.UTF_8, StandardOpenOption.SYNC,
				StandardOpenOption.WRITE);
		try {
			out.write(getProcessName());
			out.flush();
		}
		finally {
			out.close();
		}
	}

	private String getProcessName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}
}
