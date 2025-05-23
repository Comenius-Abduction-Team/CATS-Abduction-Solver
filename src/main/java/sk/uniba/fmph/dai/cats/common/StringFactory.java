package sk.uniba.fmph.dai.cats.common;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.*;
import sk.uniba.fmph.dai.cats.data.Explanation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringFactory {

    public static String getRepresentation(OWLAxiom owlAxiom) {
        if (owlAxiom instanceof OWLClassAssertionAxiom) {
            return getClassAssertionAxiom(owlAxiom).concat(DLSyntax.LEFT_PARENTHESES).
                    concat(getNamedIndividual(owlAxiom)).concat(DLSyntax.RIGHT_PARENTHESES);
        }
        return getObjectPropertyAssertionAxiom(owlAxiom);
    }

    public static String getRepresentation(Collection<OWLAxiom> axioms){
        List<String> result = new ArrayList<>();
        for (OWLAxiom owlAxiom : axioms) {
            result.add(getRepresentation(owlAxiom));
        }
        return "{" + StringUtils.join(result, ",") + "}";
    }

    public static String getRepresentation(Explanation explanation){
        List<String> result = new ArrayList<>();
        for (OWLAxiom owlAxiom : explanation.getAxioms()) {
            result.add(getRepresentation(owlAxiom));
        }
        return "{" + StringUtils.join(result, ",") + "}";
    }

    public static String getExplanationsRepresentation(Collection<Explanation> explanations){
        List<String> result = new ArrayList<>();
        if (explanations != null) {
            for (Explanation e : explanations) {
                if (e == null)
                    continue;
                result.add(getRepresentation(e));
            }
        }
        return "{ " + StringUtils.join(result, ", ") + " }";
    }

    private static String getNamedIndividual(OWLAxiom owlAxiom) {
        List<String> owlNamedIndividuals = new ArrayList<>();
        List<OWLNamedIndividual> individualsInSignature = owlAxiom.individualsInSignature().collect(Collectors.toList());

        for (OWLNamedIndividual owlNamedIndividual : individualsInSignature) {
            owlNamedIndividuals.add(owlNamedIndividual.getIRI().getFragment());
        }

        return StringUtils.join(owlNamedIndividuals, DLSyntax.DELIMITER_INDIVIDUAL);
    }

    public static String getClassAssertionAxiom(OWLAxiom owlAxiom) {
        List<OWLAxiom> axioms = new ArrayList<>();
        axioms.add(owlAxiom);

        Set<OWLAxiom> classAssertionAxioms = AxiomType.getAxiomsOfTypes(axioms, AxiomType.CLASS_ASSERTION);
        List<String> classAssertions = new ArrayList<>();

        for (OWLAxiom classAssertionAxiom : classAssertionAxioms) {
            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) classAssertionAxiom).getClassExpression();

            if (owlClassExpression instanceof OWLClass) {
                classAssertions.add(owlClassExpression.asOWLClass().getIRI().getFragment());
            } else if (owlClassExpression instanceof OWLObjectComplementOf) {
                classAssertions.add(DLSyntax.DISPLAY_NEGATION + owlClassExpression.getComplementNNF().asOWLClass().getIRI().getFragment());
            }
        }

        return StringUtils.join(classAssertions, " ");
    }

    private static String getObjectPropertyAssertionAxiom(OWLAxiom owlAxiom) {
        String property = "";
        String subject = "";
        String object = "";

        if (OWLObjectPropertyAssertionAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
            OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = (OWLObjectPropertyAssertionAxiom) owlAxiom;

            property = objectPropertyAssertionAxiom.getProperty().getNamedProperty().getIRI().getFragment();

            if (objectPropertyAssertionAxiom.getSubject().isOWLNamedIndividual()) {
                subject = objectPropertyAssertionAxiom.getSubject().asOWLNamedIndividual().getIRI().getFragment();
            }

            if (objectPropertyAssertionAxiom.getObject().isOWLNamedIndividual()) {
                object = objectPropertyAssertionAxiom.getObject().asOWLNamedIndividual().getIRI().getFragment();
            }

        } else if (OWLNegativeObjectPropertyAssertionAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
            OWLNegativeObjectPropertyAssertionAxiom negativeObjectPropertyAssertionAxiom = (OWLNegativeObjectPropertyAssertionAxiom) owlAxiom;

            property = DLSyntax.DISPLAY_NEGATION + negativeObjectPropertyAssertionAxiom.getProperty().getNamedProperty().getIRI().getFragment();

            if (negativeObjectPropertyAssertionAxiom.getSubject().isOWLNamedIndividual()) {
                subject = negativeObjectPropertyAssertionAxiom.getSubject().asOWLNamedIndividual().getIRI().getFragment();
            }

            if (negativeObjectPropertyAssertionAxiom.getObject().isOWLNamedIndividual()) {
                object = negativeObjectPropertyAssertionAxiom.getObject().asOWLNamedIndividual().getIRI().getFragment();
            }
        }

        return property.concat(DLSyntax.LEFT_PARENTHESES).concat(subject).concat(DLSyntax.DELIMITER_OBJECT_PROPERTY).concat(object).concat(DLSyntax.RIGHT_PARENTHESES);
    }

    public static String extractClassName(OWLAxiom axiom) {
        return getRepresentation(axiom).split("\\" + DLSyntax.LEFT_PARENTHESES)[0];
    }

    public static String buildCsvRow(boolean addComas, Object... objects) {
        StringBuilder builder = new StringBuilder();
        buildCsvRow(builder, addComas, objects);
        return builder.toString();

//        for (int i = 0; i < objects.length; i++) {
//            if (objects[i] != null)
//                builder.append(objects[i].toString());
//            if (i + 1 != objects.length) {
//                builder.append(';');
//                if (addComas)
//                    builder.append(' ');
//            }
//        }
//
//        return builder.toString();

    }

    public static void buildCsvRow(StringBuilder builder, boolean addComas, Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] != null)
                builder.append(objects[i].toString());
            if (i + 1 != objects.length) {
                builder.append(';');
                if (addComas)
                    builder.append(' ');
            }
        }
    }

    private static String formatTimeByPattern(long time, String pattern){
        Instant instant = Instant.ofEpochMilli(time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static String formatTimeWithDate(long time){
        return formatTimeByPattern(time, "yyyy-MM-dd-HH-mm-ss-SSS");
    }

    public static String formatTime(long time){
        return formatTimeByPattern(time, "HH:mm:ss");
    }

}
