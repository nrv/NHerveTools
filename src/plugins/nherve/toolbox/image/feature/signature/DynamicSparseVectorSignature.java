package plugins.nherve.toolbox.image.feature.signature;

public class DynamicSparseVectorSignature extends SparseVectorSignature {

	public DynamicSparseVectorSignature() {
		super(0);
	}

	@Override
	public void set(int idx, double val) throws SignatureException {
		if (idx >= size) {
			size = idx + 1;
		}
		super.set(idx, val);
	}

}
