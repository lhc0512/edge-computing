package cn.edu.scut.agent.masac;

import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.AbstractBlock;
import ai.djl.nn.Parameter;
import ai.djl.training.ParameterStore;
import ai.djl.training.initializer.ConstantInitializer;
import ai.djl.util.PairList;

public class AlphaBlock extends AbstractBlock {

    private Parameter logAlpha;

    public AlphaBlock(float initAlpha) {
        logAlpha = addParameter(Parameter.builder()
                .setName("alpha")
                .setType(Parameter.Type.WEIGHT)
                .optShape(new Shape(1))
                .optInitializer(new ConstantInitializer((float) Math.log(initAlpha)))
                .optRequiresGrad(true)
                .build());
    }

    @Override
    protected NDList forwardInternal(ParameterStore parameterStore, NDList inputs, boolean training, PairList<String, Object> params) {
        return new NDList(logAlpha.getArray());
    }

    @Override
    public Shape[] getOutputShapes(Shape[] inputShapes) {
        return new Shape[]{new Shape(1)};
    }
}
