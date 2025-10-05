package sk.uniba.fmph.dai.cats.algorithms;

public enum Algorithm {

    MHS,

    MXP {
        @Override
        public boolean usesMxp() {
            return true;
        }

        @Override
        public boolean isRootOnly() {
            return true;
        }
    },

    MHS_MXP {
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

    },

    HST_MXP {
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
    },

    RCT {
        @Override
        public boolean isRcTree() {
            return true;
        }

    },

    RCT_MXP {
        @Override
        public boolean isRcTree() {
            return true;
        }

        @Override
        public boolean usesMxp() {
            return true;
        }

        @Override
        public boolean matchesName(String name) {
            return super.matchesName(name) || "RCT-MXP".equals(name) || "RCTMXP".equals(name);
        }
    },

    QXP {
        @Override
        public boolean usesQxp() {
            return true;
        }

        @Override
        public boolean isRootOnly() {
            return true;
        }
    };

    public boolean isRootOnly(){
        return false;
    }
    public boolean isHst(){
        return false;
    }

    public boolean isRcTree(){
        return false;
    }

    public boolean usesMxp(){
        return false;
    }

    public boolean usesQxp(){
        return false;
    }

    public boolean matchesName(String name){
        return this.name().equals(name);
    }

    public Optimisation[] getDefaultOptimisationsWithoutNegations(){
        if (isRootOnly())
            return new Optimisation[0];
        if (isHst())
            return new Optimisation[]{Optimisation.SORT_MODEL};
        if (isRcTree())
            return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS};
        return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS, Optimisation.SORT_MODEL};
    }

    public Optimisation[] getDefaultOptimisationsWithNegations(){
        if (isRootOnly())
            return new Optimisation[0];
        if (!isHst() && !isRcTree())
            return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS, Optimisation.TRIPLE_MXP,
                    Optimisation.SORT_MODEL};
        return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS, Optimisation.TRIPLE_MXP};
    }

}
