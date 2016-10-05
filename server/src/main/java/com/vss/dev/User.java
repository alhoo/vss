package com.vss.dev;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Vector;

import com.github.pgasync.Row;

enum State {ACTIVE, DISABLED};

public class User extends VSSObject{
	private Integer _id;
	private Integer _gid;
	private Date _createtime;
	private String _name;
	private State _state;
	private String _pw;
	private String _email;
	public User(Row r){
		_id = r.getInt("ID");
		_name = r.getString("name");
		_state = State.valueOf(r.getString("state").toUpperCase());
		_createtime = r.getTimestamp("createtime");
		_email = r.getString("email");
//		_pw = "not available";
	}
	/*
	@JsonCreator
	public User(@JsonProperty("name") String name, @JsonProperty("pw") String pw){
		System.out.println("From json (name, pw) to User");
		_name = name;
		try {
			PasswordAuth auth = new PasswordAuth();
			_pw = auth.encode(pw);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@JsonCreator
	public User(@JsonProperty("name") String name, @JsonProperty("pw") String pw, @JsonProperty("email") String email){
		System.out.println("From json (name, pw, email) to User");
		_name = name;
		_email = email;
		try {
			PasswordAuth auth = new PasswordAuth();
			_pw = pw);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_state = State.DISABLED;
		_id = 0;
		_createtime = new Date();
	}
	*/
	/*
	public Row toDB(){
		return PgRow(DataRow data, Map<String, PgColumn> columns, DataConverter dataConverter);
	}
	*/
	public User() {
		// TODO Auto-generated constructor stub
	}
	public Integer getId() { return _id; }
	public Integer getGid() { return _gid; }
	public String getName() { return _name; }
	public String getPw() { return _pw; }
	public State getState() { return _state; }
	public Date getCreateTime() { return _createtime; }
	public String getEmail() { return _email; }

	public void setId(Integer v) {  _id = v; }
	public void setGid(Integer v) {  _gid = v; }
	public void setPw(String v) throws InvalidKeySpecException, NoSuchAlgorithmException { PasswordAuth auth = new PasswordAuth(); _pw = auth.encode(v); }
	public void setName(String v) { _name = v; }
	public void setState(State v) { _state = v; }
	public void setCreateTime(Date v) { _createtime = v; }
	//public void setCreateTime(Timestamp v) { _createtime = v; }
	public void setEmail(String v) { _email = v; }
	@Override
	protected String dbkeys(){
		return " (name, email, pw) ";
	}
	@Override
	protected String dbplaceholder(int offset){
		return _dbplaceholder_string(3, offset);
	}
	@Override
	protected String[] dbvalues(){
		return new String[]{_name, _email, _pw};
		//return new String[]{_name, _email};
	}
	@Override
	protected String getQuery(){
		return "SELECT u2.ID, u2.name, u2.state, u2.createtime, u2.email FROM users u1, groups g, users u2 where u1.ID = $1 and u1.gID = g.ID and u2.gID = g.ID and u2.ID = $2";
	}
	@Override
	protected String getAllQuery(){
		return "SELECT u2.ID, u2.name, u2.state, u2.createtime, u2.email FROM users u1, groups g, users u2 where u1.ID = $1 and u1.gID = g.ID and u2.gID = g.ID";
	}
}
