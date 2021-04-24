package ca.pfv.spmf.algorithmmanager.descriptions;

import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.sequentialpatterns.FHUSPM.FHUSPM_FNUB_Release;

public class DescriptionFHUSPM_FNUB_Release extends DescriptionOfAlgorithm {
    public DescriptionFHUSPM_FNUB_Release() {
    }

    @Override
    public String getName() {
        return "FHUSPM_FNUB_Release";
    }

    @Override
    public String getAlgorithmCategory() {
        return "Release";
    }

    @Override
    public String getURLOfDocumentation() {
        return "";
    }

    @Override
    public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws Exception {
        FHUSPM_FNUB_Release algo = new FHUSPM_FNUB_Release();
        // execute the algorithm
        algo.runAlgorithm(inputFile, outputFile,
                getParamAsDouble(parameters[0]), getParamAsInteger(parameters[1])); //
        algo.printStatistics();
    }

    @Override
    public DescriptionOfParameter[] getParametersDescription() {

        DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
        parameters[0] = new DescriptionOfParameter("Minimum utility (percent)", "(e.g. 0.2)", Double.class, false);
        parameters[1] = new DescriptionOfParameter("Minimum support", "(e.g. 4)", Integer.class, false);
        return parameters;
    }

    @Override
    public String getImplementationAuthorNames() {
        return "Hanh Thong";
    }

    @Override
    public String[] getInputFileTypes() {
        return new String[]{"Database of instances","Sequence database", "Sequence Database with utility values"};
    }

    @Override
    public String[] getOutputFileTypes() {
        return new String[]{"Patterns", "High-utility patterns","Sequential patterns", "High-utility patterns", "High-utility sequential patterns"};
    }
}
