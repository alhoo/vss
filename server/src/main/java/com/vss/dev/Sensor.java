package com.vss.dev;

import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.pgasync.Row;

public class Sensor extends VSSObject{
	private Integer _id;
	private String _location;
	private State _state;
	private Date _createtime;
	private Integer _gid;

	public Sensor(Row r){
		_id = r.getInt("ID");
		_location = r.getString("location");
		_state = State.valueOf(r.getString("state").toUpperCase());
		_createtime = r.getTimestamp("createtime");
	}
	@JsonCreator
	public Sensor(@JsonProperty("location") String location){
		_location = location;
		
		_state = State.ACTIVE;
		_id = 0;
		_createtime = new Date();
	}
	public Sensor() {
	}
	public Integer getId() { return _id; }
	public Integer getGid() { return _gid; }
	public String getName() { return _location; }
	public State getState() { return _state; }
	public Date getCreateTime() { return _createtime; }

	public void setId(Integer v) {  _id = v; }
	public void setGid(Integer v) {  _gid = v; }
	public void setLocation(String v) { _location = v; }
	public void setState(State v) { _state = v; }
	public void setCreateTime(Date v) { _createtime = v; }
	//public void setCreateTime(Timestamp v) { _createtime = v; }
	@Override
	protected String dbkeys(){
		return " (location, gID) ";
	}
	@Override
	protected String dbplaceholder(int offset){
		return _dbplaceholder_string(2, offset);
	}
	@Override
	protected String[] dbvalues(){
		return new String[]{_location, _gid.toString()};
	}
	@Override
	protected String getQuery(){
		return "SELECT s.ID, s.location, s.state, s.createtime FROM users u, groups g, sensors s where u.ID = $1 and u.gID = g.ID and s.gID = g.ID and s.ID = $2";
	}
	@Override
	protected String getAllQuery(){
		return "SELECT s.ID, s.location, s.state, s.createtime FROM users u, groups g, sensors s where u.ID = $1 and u.gID = g.ID and s.gID = g.ID";
	}
}
