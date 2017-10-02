package cn.ssd.wean2016.springsecurity.model;


import java.util.HashMap;

/**
 * 
 * @author wean2016
 *
 */
@SuppressWarnings("serial")
public class ResultMap extends HashMap<String, Object> {


	public ResultMap() {

	}

	public ResultMap success() {
		this.put("code", "200");
		return this;
	}

	public ResultMap fail(String code) {
		this.put("code", code);
		return this;
	}

	public ResultMap message(String message) {
		this.put("message", message);
		return this;
	}

	public ResultMap data(Object obj) {
		this.put("data", obj);
		return this;
	}

	public ResultMap token(String token) {
		this.put("X-Auth-Token", token);
		return this;
	}
}
