package com.vss.dev;

import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.pgasync.Row;

//enum State {ACTIVE, DISABLED};

public class Group extends VSSObject{
	private Integer _id;
	private String _name;
	private State _state;
	private Date _createtime;

	public Group(Row r){
		_id = r.getInt("ID");
		_name = r.getString("name");
		_state = State.valueOf(r.getString("state").toUpperCase());
		_createtime = r.getTimestamp("createtime");
	}
	@JsonCreator
	public Group(@JsonProperty("name") String name){
		_name = name;
		
		_state = State.ACTIVE;
		_id = 0;
		_createtime = new Date();
	}
	public Group() {
		// TODO Auto-generated constructor stub
	}
	public Integer getId() { return _id; }
	public String getName() { return _name; }
	public State getState() { return _state; }
	public Date getCreateTime() { return _createtime; }

	public void setId(Integer v) {  _id = v; }
	public void setName(String v) { _name = v; }
	public void setState(State v) { _state = v; }
	public void setCreateTime(Date v) { _createtime = v; }
	//public void setCreateTime(Timestamp v) { _createtime = v; }
	
	@Override
	protected String dbkeys(){
		return " (name) ";
	}
	@Override
	protected String dbplaceholder(int offset){
		return _dbplaceholder_string(1, offset);
	}
	@Override
	protected String[] dbvalues(){
		return new String[]{_name};
	}
	@Override
	protected String getQuery(){
		return "SELECT g.ID, g.name, g.state, g.createtime FROM users u, groups g where u.ID = $1 and u.gID = g.ID and g.ID = $2";
	}
	@Override
	protected String getAllQuery(){
		return "SELECT g.ID, g.name, g.state, g.createtime FROM users u, groups g where u.ID = $1 and u.gID = g.ID";
	}
}
