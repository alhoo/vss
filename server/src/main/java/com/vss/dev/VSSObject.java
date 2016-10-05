package com.vss.dev;

import java.util.Vector;

public abstract class VSSObject {
	protected abstract Integer getId();
	protected abstract String dbkeys();
	
	protected String _dbplaceholder_string(int _len, int offset){
		Vector<String> parts = new Vector<String>();
		for(int i = 0; i < _len; i++){
			parts.add("$".concat(Integer.toString(1 + offset + i)));
		}
		StringBuilder ret = new StringBuilder(" (");
			ret.append(String.join(", ", parts));
			ret.append(") ");
		return ret.toString();
	}
	protected abstract String dbplaceholder(int offset);
	protected String dbplaceholder(){ return dbplaceholder(0);}
	protected abstract String[] dbvalues();
	protected abstract String getQuery();
	protected abstract String getAllQuery();
}
