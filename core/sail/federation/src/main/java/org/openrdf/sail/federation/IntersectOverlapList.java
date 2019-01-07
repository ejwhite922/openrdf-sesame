package org.openrdf.sail.federation;


import java.util.Set;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;

public class IntersectOverlapList <E , X extends Exception> extends FilterIteration<E, X> {

	private Set<String>includeSet;

		public IntersectOverlapList(Iteration<? extends E, ? extends X> iter, Set<String> includeSet) throws X  {
		super(iter);	
      this.includeSet=includeSet;


	}
	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>true</tt> if the specified object hasn't been seen before.
	 */
	@Override
	/*
	protected boolean accept(E object)
			throws X
		{
		System.out.println(excludeSet.size());
			if (inExcludeSet(object)) {
				// object has already been returned
				return false;
			}
			else {
				add(object);
				System.out.println(object);
				return true;
			}
		}
		*/
	
	protected boolean accept(E object)
		throws X
	{
		if (inIncludeSet(object)) {

			return true;
		}
		else {

			return false;
		}
	}

	/**
	 * @param object
	 * @return true if the object is in the excludeSet
	 */

	
	private boolean inIncludeSet(E object) { 
		Resource sub =((ContextStatementImpl)(object)).getSubject();
		Value obj = ((ContextStatementImpl)(object)).getObject();
	
    if( includeSet.contains(sub.stringValue()) || includeSet.contains(obj.stringValue())){
   //	 System.out.println(subj);
   	 return true;
    }else{
	return false;
    }

	}
	
}