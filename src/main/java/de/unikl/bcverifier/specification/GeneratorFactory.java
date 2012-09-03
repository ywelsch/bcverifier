package de.unikl.bcverifier.specification;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class GeneratorFactory {
    public static Generator getGenerator(Configuration config, TwoLibraryModel twoLibraryModel) throws GenerationException {
        switch(config.specificationType()){
        case BSL:
            return new BoogieGenerator(config);
        case ISL:
        	return new ISLGenerator(config, twoLibraryModel);
        default:
            throw new GenerationException("Generator of type "+config.specificationType()+" not supported yet.");
        }
    }
}
