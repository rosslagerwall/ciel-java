package com.asgow.ciel.references;

import java.io.IOException;
import java.io.OutputStream;

import com.asgow.ciel.executor.Ciel;
import com.asgow.ciel.io.CielOutputStream;
import com.google.gson.*;

public class WritableReference {

	private final String filename;
	private final int outputIndex;
	private final boolean mayOmitSize;
	private Reference completeRef;
	private boolean isClosed;

	// If this is set, then the reference will get closed when the spawn RPC
	// method is called, and so close() shouldn't do much.
	private boolean isSpawnClose;

	private long final_size = -1;
	
	public WritableReference(String filename, int outputIndex, boolean may_omit_size) {
		this.filename = filename;
		this.outputIndex = outputIndex;
		this.mayOmitSize = may_omit_size;
		this.isClosed = false;
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public void close(long final_size) {
		if (this.isClosed) {
			throw new RuntimeException("Double close on output " + this.outputIndex);
		}
		this.isClosed = true;
		if (isSpawnClose) {
			this.final_size = final_size;
			return;
		}
		this.completeRef = Ciel.RPC.closeOutput(this.outputIndex, final_size);
	}
	
	public void close() {
		if(this.isClosed) {
			throw new RuntimeException("Double close on output " + this.outputIndex);
		}
		this.isClosed = true;
		if (isSpawnClose) {
			return;
		}
		if(!this.mayOmitSize) {
			throw new RuntimeException("Must specify a size when closing WritableReference, as it was opened with may_pipe=true");
		}
		else {
			this.completeRef = Ciel.RPC.closeOutput(this.outputIndex);
		}
	}

	public void setSpawnClose(boolean val) {
		this.isSpawnClose = val;
	}

	public long getFinalSize() {
		return this.final_size;
	}

	public JsonObject toJson() {
		JsonObject ret = new JsonObject();
		ret.add("index", new JsonPrimitive(this.outputIndex));
		ret.add("size", new JsonPrimitive(this.final_size));
		return ret;
	}
	
	public Reference getCompletedRef() {
		return this.completeRef;
	}
	
	public int getIndex() {
		return this.outputIndex;
	}
	
	public OutputStream open() throws IOException {
		return new CielOutputStream(this);
	}
	
}
