package org.openrdf.sail.federation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;

import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;

/**
 * The overlap list that holds the IRIs or literals in the current cluster that
 * also appear as subject or object in another cluster(s).
 * @param <E> the type to iterate over.
 * @param <X> the type of {@code Exception} to throw.
 */
public class IntersectOverlapList<E, X extends Exception> extends FilterIteration<E, X> {
    private final Set<String> includeSet;

    /**
     * Creates a new instance of {@link IntersectOverlapList}.
     * @param iter the {@link Iteration}
     * @param includeSet the include set. (not null)
     * @throws X
     */
    public IntersectOverlapList(final Iteration<? extends E, ? extends X> iter, final Set<String> includeSet) throws X {
        super(iter);
        this.includeSet = checkNotNull(includeSet);
    }

    @Override
    protected boolean accept(final E object) throws X {
        return inIncludeSet(object);
    }

    /**
     * Checks if the object is in the include set.
     * @param object the typed object to check for inclusion in the set.
     * @return {@code true} if the object is in the includeSet.
     * {@false} otherwise.
     */
    private boolean inIncludeSet(final E object) {
        final Resource sub = ((ContextStatementImpl)(object)).getSubject();
        final Value obj = ((ContextStatementImpl)(object)).getObject();

        return includeSet.contains(sub.stringValue()) || includeSet.contains(obj.stringValue());
    }
}