package plugins.nherve.toolbox.image.feature.signature;

public class WeightedDenseVectorSignature extends DenseVectorSignature implements WeightedVectorSignature {
	private double weight;

	public WeightedDenseVectorSignature() {
		super();
	}

	public WeightedDenseVectorSignature(double[] data) {
		super(data);
	}

	public WeightedDenseVectorSignature(float[] data) {
		super(data);
	}

	public WeightedDenseVectorSignature(int size) {
		super(size);
	}

	public WeightedDenseVectorSignature(int size, double initialValue) {
		super(size, initialValue);
	}

	@Override
	public double getWeight() {
		return weight;
	}

	public WeightedDenseVectorSignature setWeight(double weight) {
		this.weight = weight;
		return this;
	}

}
