package entity;

import java.io.Serializable;
import java.util.Objects;

public class UserEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	private String username, password, first_name, last_name, email, phone_number, role_type, region;
	private boolean logged_in, isNotApproved;
	private int id;

	@Override
	public String toString() {
		return username;
	}

	public UserEntity(String username, String password, String first_name, String last_name, String email,
			String phone_number, String role_type, String region, boolean logged_in, boolean isNotApproved) {
		this(username, password, first_name, last_name, email, phone_number, role_type, logged_in, isNotApproved);

		this.region = region;
	}

	public UserEntity(String username, String password, String first_name, String last_name, String email,
			String phone_number, String role_type, boolean logged_in, boolean isNotApproved) {
		this.username = username;
		this.password = password;
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.phone_number = phone_number;
		this.role_type = role_type;
		this.isNotApproved = isNotApproved;
		this.logged_in = false;
	}

	public String friendlyName() {
		return role_type + " " + first_name + " " + last_name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserEntity other = (UserEntity) obj;
		return Objects.equals(username, other.username);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getRole_type() {
		return role_type;
	}

	public void setRole_type(String role_type) {
		this.role_type = role_type;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public boolean isLogged_in() {
		return logged_in;
	}

	public void setLogged_in(boolean logged_in) {
		this.logged_in = logged_in;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isNotApproved() {
		return isNotApproved;
	}

	public void setNotApproved(boolean isApproved) {
		this.isNotApproved = isApproved;
	}

}
