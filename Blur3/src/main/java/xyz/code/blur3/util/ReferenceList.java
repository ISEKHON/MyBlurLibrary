package xyz.code.blur3.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list that holds weak references to objects
 */
public class ReferenceList<T> implements Iterable<T> {
    private final ArrayList<WeakReference<T>> list = new ArrayList<>();
    
    public void add(T item) {
        list.add(new WeakReference<>(item));
    }
    
    public void remove(T item) {
        Iterator<WeakReference<T>> iterator = list.iterator();
        while (iterator.hasNext()) {
            WeakReference<T> ref = iterator.next();
            T obj = ref.get();
            if (obj == null || obj == item) {
                iterator.remove();
            }
        }
    }
    
    public void clear() {
        list.clear();
    }
    
    public int size() {
        // Clean up null references
        Iterator<WeakReference<T>> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().get() == null) {
                iterator.remove();
            }
        }
        return list.size();
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<WeakReference<T>> weakIterator = list.iterator();
            private T next;
            
            @Override
            public boolean hasNext() {
                while (weakIterator.hasNext()) {
                    WeakReference<T> ref = weakIterator.next();
                    T obj = ref.get();
                    if (obj != null) {
                        next = obj;
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public T next() {
                return next;
            }
        };
    }
}
