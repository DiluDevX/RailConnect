package com.railway.complaintmanagement.dao;

import com.railway.complaintmanagement.model.Complaint;

import java.util.List;
import java.util.Optional;

public interface ComplaintDao {
    Complaint save(Complaint complaint);
    Optional<Complaint> findById(String complaintId);
    List<Complaint> findAll();
    List<Complaint> findByUserId(String userId);
    List<Complaint> findByStatus(String status);
    boolean update(Complaint complaint);
    boolean delete(String complaintId);
}
