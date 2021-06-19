package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {
	
	private Connection conn;
	
	public DepartmentDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Department department) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"insert into DEPARTMENT ([NAME]) "
					+ "values (?)",
					Statement.RETURN_GENERATED_KEYS
					);
			
			st.setString(1, department.getName());
			
			int rowsAffected = st.executeUpdate();
			
			if(rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1);
					department.setId(id);
				}
				DB.closeResultSet(rs);
			}else {
				throw new DbException("Unexpected error! No rows affected!");
			}
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public void update(Department department) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"update DEPARTMENT "
					+ "set [NAME] = ? "
					+ "where ID = ?"
					);
			
			st.setString(1, department.getName());
			st.setInt(2, department.getId());
			
			st.executeUpdate();
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			
		}		
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"delete from DEPARTMENT "
					+ "where ID = ?"
					);
			st.setInt(1, id);
			
			st.executeUpdate();
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);;
		}		
	}

	@Override
	public Department findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"select * from DEPARTMENT "
					+ "where ID = ?"
					);
			
			st.setInt(1, id);
			
			rs = st.executeQuery();
			
			if(rs.next()) {
				Department department = instantiateDepartment(rs);
				return department;
			}
			
			return null;
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());			
		}finally {
			DB.closeStatement(st);
			DB.closeStatement(st);
		}
		
	}

	@Override
	public List<Department> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {			
			st = conn.prepareStatement(
					"select * from DEPARTMENT order by [NAME]"
					);
			
			rs = st.executeQuery();
			List<Department> list = new ArrayList<>();
			Map<Integer, Department> deps = new HashMap<>();
			
			while(rs.next()) {
				Department department = deps.get(rs.getInt("ID"));
				
				if(department != null) {
					department = instantiateDepartment(rs);
					deps.put(rs.getInt("ID"), department);
				}
				
				Department dep = instantiateDepartment(rs);
				
				list.add(dep);
			}
			
			return list;
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
	
	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department department = new Department();
		department.setId(rs.getInt("ID"));
		department.setName(rs.getString("NAME"));
		return department;
	}
	

}
