package plugins.nherve.toolbox.image.feature.signature;

public class WeightedSparseVectorSignature extends SparseVectorSignature implements WeightedVectorSignature {
	private double weight;

	public WeightedSparseVectorSignature(int size) {
		super(size);
	}

	@Override
	public double getWeight() {
		return weight;
	}

	public WeightedSparseVectorSignature setWeight(double weight) {
		this.weight = weight;
		return this;
	}

}
