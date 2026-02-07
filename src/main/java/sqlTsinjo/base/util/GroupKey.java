package sqlTsinjo.base.util;
import java.util.Arrays;
import java.util.Vector;

import sqlTsinjo.base.Relation;
 public  final class GroupKey {
        private final Vector<Object> values;

        public GroupKey(Vector<Object> values) {
            this.values = values;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof GroupKey other))
                return false;
            if (this.values.size() != other.values.size())
                return false;
            for (int i = 0; i < this.values.size(); i++) {
                if (!Relation.objectsEqual(this.values.get(i), other.values.get(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (Object v : values) {
                int h;
                if (v == null) {
                    h = 0;
                } else if (v instanceof char[] c) {
                    h = Arrays.hashCode(c);
                } else {
                    h = v.hashCode();
                }
                result = 31 * result + h;
            }
            return result;
        }
    }