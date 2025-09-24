package sk.uniba.fmph.dai.cats.algorithms;

public enum Optimisation {

    MOVE_CONSISTENCY_CHECKS { // move consistency checks for irrelevant/inconsistent explanations ater model reuse
        @Override
        public String getDescription() {
            return "fewer conistency checks";
        }
    },
    SORT_MODEL { // sort models as they are stored for reuse based on the negated model's size
        @Override
        public String getDescription() {
            return "sort models";
        }
    },
    REMOVE_NEGATED_PATH { // remove negated version of current path from MXP's input abducibles
        @Override
        public String getDescription() {
            return "remove negated path from MXP abducibles";
        }
    },
    TRIPLE_MXP { // run MXP once with positive abducibles, once with negative abducibles, and once with all of them
        @Override
        public String getDescription() {
            return "triple MXP";
        }
    };

    public String getDescription(){return "";}

}
