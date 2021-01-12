package com.alipay.sofa.registry.comparator;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class Triple<F, M, L> {
    
    private volatile F first;
    
    private volatile M middle;
    
    private volatile L last;

    public Triple() {
    }

    public Triple(F first, M middle, L last) {
        this.first = first;
        this.middle = middle;
        this.last = last;
    }

    public static <F, M, L> Triple<F, M, L> from(F first, M middle, L last) {
        return new Triple<F, M, L>(first, middle, last);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Triple) {
            Triple<Object, Object, Object> o = (Triple)obj;
            if (this.first == null) {
                if (o.first != null) {
                    return false;
                }
            } else if (!this.first.equals(o.first)) {
                return false;
            }

            if (this.middle == null) {
                if (o.middle != null) {
                    return false;
                }
            } else if (!this.middle.equals(o.middle)) {
                return false;
            }

            if (this.last == null) {
                if (o.last != null) {
                    return false;
                }
            } else if (!this.last.equals(o.last)) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public F getFirst() {
        return this.first;
    }

    public L getLast() {
        return this.last;
    }

    public M getMiddle() {
        return this.middle;
    }

    public int hashCode() {
        int hash = (this.first == null ? 0 : this.first.hashCode());
        hash = hash * 31 + (this.middle == null ? 0 : this.middle.hashCode());
        hash = hash * 31 + (this.last == null ? 0 : this.last.hashCode());
        return hash;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setLast(L last) {
        this.last = last;
    }

    public void setMiddle(M middle) {
        this.middle = middle;
    }

    public int size() {
        return 3;
    }

    public String toString() {
        return String.format("Triple[first=%s, middle=%s, last=%s]", this.first, this.middle, this.last);
    }
}
