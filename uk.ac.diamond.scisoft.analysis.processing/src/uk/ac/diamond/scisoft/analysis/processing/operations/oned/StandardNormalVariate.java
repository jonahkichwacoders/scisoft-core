package uk.ac.diamond.scisoft.analysis.processing.operations.oned;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.AbstractOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

import uk.ac.diamond.scisoft.analysis.processing.operations.EmptyModel;

public class StandardNormalVariate extends AbstractOperation<EmptyModel, OperationData> {

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.oned";
	}
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		double mean = (Double) input.mean(true);
		double std = (Double) ((Dataset)input).stdDeviation();
		Dataset output = Maths.subtract(input,mean);
		output.idivide(std);
		
		Dataset eb = (Dataset)input.getError().getSlice();
		//transfer axes
		copyMetadata(input, output);
		
		if (eb != null) {
			output.setError(Maths.divide(eb, std));
		}
		
		return new OperationData(output);
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ONE;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}

}