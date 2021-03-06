package uk.ac.diamond.scisoft.analysis.processing.runner;

import java.io.Serializable;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationRunner;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.metadata.OriginMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceVisitor;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;

/**
 * Runs a pipeline by looping the services of operations.
 * 
 * This will not work unless the pipeline is a linear series of operations 
 * with one slug running the length.
 * 
 * If you have averaging or branching, you will need to consider using a 
 * graph to execute your operations.
 * 
 * @author fcp94556
 *
 */
public class SeriesRunner implements IOperationRunner {


	private IOperationContext context;

	public void init(IOperationContext context) {
		this.context        = context;
	}

	@Override
	public void execute() throws Exception {
		final IExecutionVisitor visitor = context.getVisitor() ==null ? new IExecutionVisitor.Stub() : context.getVisitor();

		// determine data axes to populate origin metadata
		SourceInformation ssource = null; 
		
		try {
			 ssource = context.getData().getMetadata(SliceFromSeriesMetadata.class).get(0).getSourceInfo();
		} catch (Exception e) {
			logger.error("Source not obtainable. Hope this is just a unit test...");
		}
		
		final SourceInformation finalSource = ssource;
		
		// Create the slice visitor
		SliceVisitor sv = new SliceVisitor() {

			@Override
			public void visit(IDataset slice, Slice[] slices, int[] shape) throws Exception {

				List<SliceFromSeriesMetadata> meta = slice.getMetadata(SliceFromSeriesMetadata.class);
				SliceFromSeriesMetadata ssm = meta!=null && meta.size()>0 ? meta.get(0) : null;
				SliceFromSeriesMetadata fullssm = null;
				if (ssm!=null) {
					fullssm = new SliceFromSeriesMetadata(finalSource, ssm.getSliceInfo());
					slice.setMetadata(fullssm);
				}

				if (context.getMonitor() != null && context.getMonitor().isCancelled()) return;

				SourceInformation si = fullssm!=null ? fullssm.getSourceInfo() : null;
				String path = si == null ? "" : si.getFilePath();
				if (path == null) path = "";
				
				String current = "";
				if (fullssm != null) {
					try {
						current = Slice.createString(ssm.getSliceFromInput());
					} catch (Exception e) {
						//ignore
					}
				}

				OperationData  data = new OperationData(slice, (Serializable[])null);
				long start = System.currentTimeMillis();
				for (IOperation<?,?> i : context.getSeries()) {

					if (context.getMonitor()!=null) {
						context.getMonitor().subTask(path +" : " + i.getName());
					}

					OperationData tmp = i.execute(data.getData(), context.getMonitor());
					//TODO only set metadata if doesnt already contain it!
					//TODO continue if null;
					
					if (tmp == null) {
						data = null;
						break;
					}
					
					List<SliceFromSeriesMetadata> md = tmp.getData().getMetadata(SliceFromSeriesMetadata.class);
					
					if (md == null || md.isEmpty())  {
						tmp.getData().setMetadata(fullssm);
					} else {
						fullssm = md.get(0);
					}
					
					visitor.notify(i, tmp); // Optionally send intermediate result
					data = i.isPassUnmodifiedData() ? data : tmp;
				}
				logger.debug("Slice " + current + " ran in: " +(System.currentTimeMillis()-start)/1000. + " s : Thread" +Thread.currentThread().toString());
				if (data == null) return;
				visitor.executed(data, context.getMonitor()); // Send result.
				if (context.getMonitor() != null) context.getMonitor().worked(1);
			}

			@Override
			public boolean isCancelled() {
				return context.getMonitor()!=null ? context.getMonitor().isCancelled() : false;
			}
		};

		visitor.init(context.getSeries(), context.getData());
		long start = System.currentTimeMillis();
		// Jake's slicing from the conversion tool is now in Slicer.
		if (context.getExecutionType()==ExecutionType.SERIES) {
			Slicer.visitAll(context.getData(), context.getSlicing(), "Slice", sv);

		} else if (context.getExecutionType()==ExecutionType.PARALLEL) {
			Slicer.visitAllParallel(context.getData(), context.getSlicing(), "Slice", sv, context.getParallelTimeout());

		} else {
			throw new OperationException(context.getSeries()[0], "The edges are needed to execute a graph using ptolemy!");
		}
		logger.debug("Data ran in: " +(System.currentTimeMillis()-start)/1000. + " s");
		
	}

	@Override
	public ExecutionType[] getExecutionTypes() {
		return new ExecutionType[]{ExecutionType.SERIES, ExecutionType.PARALLEL};
	}

}
