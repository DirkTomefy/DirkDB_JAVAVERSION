package sqlTsinjo.base.util;

public  final class GroupAcc {
        private final Object[] selectGroupValues;
        private final AggState[] aggs;

        public GroupAcc(int selectSize, AggState[] aggs) {
            this.selectGroupValues = new Object[selectSize];
            this.aggs = aggs;
        }

        public Object[] getSelectGroupValues() {
            return selectGroupValues;
        }

        public Object getSelectGroupValue(int index) {
            return selectGroupValues[index];
        }

        public void setSelectGroupValue(int index, Object value) {
            selectGroupValues[index] = value;
        }

        public AggState[] getAggs() {
            return aggs;
        }

        public AggState getAgg(int index) {
            return aggs[index];
        }
    }
