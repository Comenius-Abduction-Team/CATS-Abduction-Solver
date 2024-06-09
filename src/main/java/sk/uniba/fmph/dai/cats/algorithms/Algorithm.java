package sk.uniba.fmph.dai.cats.algorithms;

public enum Algorithm {

    MHS, MXP {

        @Override
        public boolean usesMxp() {
            return true;
        }

    }, MHS_MXP {
        @Override
        public boolean usesMxp() {
            return true;
        }

        @Override
        public boolean matchesName(String name) {
            return super.matchesName(name) || "MHS-MXP".equals(name) || "MHSMXP".equals(name);
        }
    },

    HST {
        @Override
        public boolean isHst() {
            return true;
        }

    }, HST_MXP {
        @Override
        public boolean isHst() {
            return true;
        }

        @Override
        public boolean usesMxp() {
            return true;
        }

        @Override
        public boolean matchesName(String name) {
            return super.matchesName(name) || "HST-MXP".equals(name) || "HSTMXP".equals(name);
        }
    };

    public boolean isHst(){
        return false;
    }

    public boolean usesMxp(){
        return false;
    }

    public boolean matchesName(String name){
        return this.name().equals(name);
    }



}
