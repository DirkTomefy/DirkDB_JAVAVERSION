package sqlTsinjo.base.util;

public final class AggState {
        private String fn;
        private int columnIndex;
        private long count;
        private double sum;

        public AggState(String fn, int columnIndex) {
            this.fn = fn;
            this.columnIndex = columnIndex;
        }

        public String getFn() {
            return fn;
        }

        public void setFn(String fn) {
            this.fn = fn;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getSum() {
            return sum;
        }

        public void setSum(double sum) {
            this.sum = sum;
        }

        public void incrementCount() {
            this.count++;
        }

        public void addToSum(double value) {
            this.sum += value;
        }
    }
