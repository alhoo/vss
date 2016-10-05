package com.vss.dev;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.pgasync.Row;

//enum State {ACTIVE, DISABLED};

public class Event extends VSSObject{
	private Integer _id;
	private String _type;
	private String _url;
	private State _state;
	private Date _createtime;
	private Integer _sid;

	public Event(Row r){
		_id = r.getInt("ID");
		_url = r.getString("url");
		_sid = r.getInt("SID");
		_type = r.getString("type");
		_state = State.valueOf(r.getString("state").toUpperCase());
		_createtime = r.getTimestamp("createtime");
	}
	@JsonCreator
	public Event(@JsonProperty("type") String type, @JsonProperty("sid") Integer sid, @JsonProperty("url") String url){
		_type = type;
		_url = url;
		_sid = sid;
		
		_state = State.ACTIVE;
		_id = 0;
		_createtime = new Date();
	}
	public Event() {
		// TODO Auto-generated constructor stub
	}
	public Integer getId() { return _id; }
	public Integer getSID() { return _sid; }
	public String getType() { return _type; }
	public String getUrl() { return _url; }
	public State getState() { return _state; }
	public Date getCreateTime() { return _createtime; }

	public void setId(Integer v) {  _id = v; }
	public void setSID(Integer v) {  _sid = v; }
	public void setType(String v) { _type = v; }
	public void setUrl(String v) { _url = v; }
	public void setState(State v) { _state = v; }
	public void setCreateTime(Date v) { _createtime = v; }
	//public void setCreateTime(Timestamp v) { _createtime = v; }
	
	@Override
	protected String dbkeys(){
		return " (type, url, sID) ";
	}
	@Override
	protected String dbplaceholder(int offset){
		return _dbplaceholder_string(3, offset);
	}
	@Override
	protected String[] dbvalues(){
		return new String[]{_type, _url, _sid.toString()};
	}
	@Override
	protected String getQuery(){
		return "SELECT e.ID, e.url, e.sID, e.type, e.state, e.createtime FROM users u, groups g, sensors s, events e where u.ID = $1 and u.gID = g.ID and s.gID = g.ID and e.sID = s.ID and e.ID = $2";
	}
	@Override
	protected String getAllQuery(){
		return "SELECT e.ID, e.url, e.sID, e.type, e.state, e.createtime FROM users u, groups g, sensors s, events e where u.ID = $1 and u.gID = g.ID and s.gID = g.ID and e.sID = s.ID";
	}
}
