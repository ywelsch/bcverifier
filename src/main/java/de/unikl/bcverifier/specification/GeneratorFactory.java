package de.unikl.bcverifier.specification;

import de.unikl.bcverifier.Configuration;

public class GeneratorFactory {
    public static Generator getGenerator(Configuration config) throws GenerationException {
        switch(config.specificationType()){
        case BPL:
            return new BoogieGenerator(config);
        default:
            throw new GenerationException("Generator of type "+config.specificationType()+" not supported yet.");
        }
    }
}
