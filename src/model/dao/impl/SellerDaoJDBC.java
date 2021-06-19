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
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {
	
	private Connection conn;
	
	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller seller) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"insert into SELLER ([NAME], EMAIL, BIRTHDATE, BASESALARY, DEPARTMENTID) "
					+ "values (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS
					);
			
			st.setString(1, seller.getName());
			st.setString(2, seller.getEmail());
			st.setDate(3, new java.sql.Date(seller.getBirthDate().getTime()));
			st.setDouble(4, seller.getBaseSalaty());
			st.setInt(5, seller.getDepartment().getId());
			
			int rowsAffected = st.executeUpdate();
			
			if(rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1);
					seller.setId(id);
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
	public void update(Seller seller) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"update SELLER "
					+ "set [NAME] = ?, EMAIL = ?, BIRTHDATE = ?, BASESALARY = ?, DEPARTMENTID = ? "
					+ "where ID = ? "
					);
			
			st.setString(1, seller.getName());
			st.setString(2, seller.getEmail());
			st.setDate(3, new java.sql.Date(seller.getBirthDate().getTime()));
			st.setDouble(4, seller.getBaseSalaty());
			st.setInt(5, seller.getDepartment().getId());
			st.setInt(6, seller.getId());
			
			st.executeUpdate();
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"delete from SELLER "
					+ "where ID = ? "
					);
			
			st.setInt(1, id);
			
			st.executeUpdate();
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"select s.*, d.NAME as DEPNAME "
					+ "from SELLER s "
					+ "join DEPARTMENT d on d.ID = s.DEPARTMENTID "
					+ "where s.ID = ?"					
					);
			
			st.setInt(1, id);
			rs = st.executeQuery();
			if(rs.next()) {
				
				Department department = instantiateDepartment(rs);
				Seller seller = instantiateSeller(rs, department);
				
				return seller;
			}
			return null;
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}


	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"select s.*, d.NAME as DEPNAME "
					+ "from SELLER s "
					+ "join DEPARTMENT d on d.ID = s.DEPARTMENTID "
					+ "order by d.NAME"				
					);
			
			rs = st.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while(rs.next()) {
				
				Department dep = map.get(rs.getInt("DEPARTMENTID"));
				
				if(dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DEPARTMENTID"), dep);
				}			
				
				Seller seller = instantiateSeller(rs, dep);
				
				list.add(seller);				
				
			}
			return list;
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"select s.*, d.NAME as DEPNAME "
					+ "from SELLER s "
					+ "join DEPARTMENT d on d.ID = s.DEPARTMENTID "
					+ "where d.ID = ? "
					+ "order by d.NAME"				
					);
			
			st.setInt(1, department.getId());
			rs = st.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while(rs.next()) {
				
				Department dep = map.get(rs.getInt("DEPARTMENTID"));
				
				if(dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DEPARTMENTID"), dep);
				}			
				
				Seller seller = instantiateSeller(rs, dep);
				
				list.add(seller);				
				
			}
			return list;
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
	
	private Seller instantiateSeller(ResultSet rs, Department department) throws SQLException {
		Seller seller = new Seller();
		seller.setId(rs.getInt("ID"));
		seller.setName(rs.getString("NAME"));
		seller.setEmail(rs.getString("EMAIL"));
		seller.setBaseSalaty(rs.getDouble("BASESALARY"));
		seller.setBirthDate(rs.getDate("BIRTHDATE"));
		seller.setDepartment(department);
		return seller;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department department = new Department();
		department.setId(rs.getInt("DEPARTMENTID"));
		department.setName(rs.getString("DEPNAME"));
		return department;
	}

}
