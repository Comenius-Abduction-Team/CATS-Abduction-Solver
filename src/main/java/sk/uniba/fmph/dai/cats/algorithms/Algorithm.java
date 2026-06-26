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
            return super.matchesName(name) || "MHS-MXP".equals(name) || "MHSMXP".equals(name)
                        || "MHS_X".equals(name) || "MHS-X".equals(name) || "MHSX".equals(name);
        }
    },

    HST {
        @Override
        public boolean isHst() {
            return true;
        }

    },

    HSDAG{
        @Override
        public boolean isHsdag() {return true;}

        @Override
        public boolean matchesName(String name) {
            return "HS-DAG".equals(name) || "HS_DAG".equals(name);
        }
    },
    HSDAG_MXP{
        @Override
        public boolean isHsdag() {return true;}

        @Override
        public boolean usesMxp() {
            return true;
        }

        @Override
        public boolean matchesName(String name) {
            return  "HSDAG-MXP".equals(name) || "HSDAGMXP".equals(name);
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
            return super.matchesName(name) || "HST-MXP".equals(name) || "HSTMXP".equals(name)
                    || "HST_X".equals(name) || "HST-X".equals(name) || "HSTX".equals(name);
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
            return super.matchesName(name) || "RCT-MXP".equals(name) || "RCTMXP".equals(name)
                    || "RCT_X".equals(name) || "RCT-X".equals(name) || "RCTX".equals(name);
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

    public boolean isHsdag(){ return false; }

    /**
     * Decides which strings can be used as the value for the -alg input parameter to identify the given algorithm.
     * The default method checks whether the string matches the enum value name as it is.
     * Can be overridden to also include alternative names.
     *
     * @param name a string passed from the argument parser, always in ALL CAPS
     * @return whether the given string corresponds to this algorithm
     * */
    public boolean matchesName(String name){
        return this.name().equals(name);
    }

    public Optimisation[] getDefaultOptimisationsWithoutNegations(){
        //MXP, QXP
        if (isRootOnly())
            return new Optimisation[0];
        //HST, HST-MXP
        if (isHst())
            return new Optimisation[]{Optimisation.SORT_MODEL};
        //RCT, RCT-MXP
        if (isRcTree())
            return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS};
        //MHS, MHS-MXP
        return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS, Optimisation.SORT_MODEL};
    }

    public Optimisation[] getDefaultOptimisationsWithNegations(){
        //MXP, QXP
        if (isRootOnly())
            return new Optimisation[0];
        //MHS, MHS-MXP
        if (!isHst() && !isRcTree())
            return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS, Optimisation.TRIPLE_MXP,
                    Optimisation.SORT_MODEL};
        //HST, HST-MXP, RCT, RCT-MXP
        return new Optimisation[]{Optimisation.MOVE_CONSISTENCY_CHECKS, Optimisation.TRIPLE_MXP};
    }

}
