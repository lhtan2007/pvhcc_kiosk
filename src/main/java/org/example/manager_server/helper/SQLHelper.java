package org.example.manager_server.helper;

import org.example.shared.helper.PwdHashHelper;
import org.example.shared.model.Account;
import org.example.shared.model.CitizenRequest;
import org.example.shared.model.Department;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.sql.SQLDataException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class SQLHelper {
    private static final SessionFactory sessionFactory = buildSessionFactory();
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        }
        catch(Exception e) {
            System.err.println("Khởi tạo SessionFactory thất bại: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public static List<Department> getAllDepartments() {
        try(Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Department", Department.class).list();
        }
        catch(Exception e) {
            System.err.println("Có lỗi khi lấy danh sách đơn vị: " + e.getMessage());
            return null;
        }
    }

    public static Department getDepartment(UUID departmentId) {
        try(Session session = sessionFactory.openSession()) {
            Query<Department> departmentQuery = session.createQuery("FROM Department WHERE departmentId = :departmentId", Department.class);
            departmentQuery.setParameter("departmentId", departmentId);
            return departmentQuery.getSingleResultOrNull();
        }
        catch(Exception e) {
            System.err.println("Có lỗi khi lấy thông tin đơn vị: " + e.getMessage());
            return null;
        }
    }

    public static boolean addDepartment(String departmentName, int maxConcurrentRequestInDay) {
        boolean isCompleted = false;
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Department newDepartment = new Department(departmentName, 0, maxConcurrentRequestInDay);
                session.persist(newDepartment);
                tx.commit();
                isCompleted = true;
            }
            catch(Exception e) {
                if (tx != null) tx.rollback();
                System.err.println("Lỗi khi thêm đơn vị: " + e.getMessage());
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return isCompleted;
    }

    public static boolean editDepartment(UUID departmentId, String departmentName, int maxConcurrentRequestInDay) {
        boolean isCompleted = false;
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Department dept = getDepartment(departmentId);
                if(dept != null) {
                    dept.setDepartmentName(departmentName);
                    dept.setMaxConcurrentRequestInDay(maxConcurrentRequestInDay);
                    session.merge(dept);
                    tx.commit();
                    isCompleted = true;
                }
            }
            catch(Exception e) {
                if(tx != null) tx.rollback();
                System.err.println("Lỗi khi sửa cấu hình đơn vị: " + e.getMessage());
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return isCompleted;
    }

    public static boolean deleteDepartment(UUID departmentId) {
        boolean isCompleted = false;
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                Department dept = getDepartment(departmentId);
                if(dept != null) {
                    session.remove(dept);
                    tx.commit();
                    isCompleted = true;
                }
            }
            catch(Exception e) {
                if(tx != null) tx.rollback();
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return isCompleted;
    }

    public static CitizenRequest getNewestCtzRequest(Department dept) {
        try(Session session = sessionFactory.openSession()) {
            Query<CitizenRequest> requestQuery = session.createQuery(
                    "FROM CitizenRequest WHERE departmentId = :departmentId " +
                            "AND requestDate >= :requestDate AND processStatus = 0" +
                            "ORDER BY requestDate ASC",
                    CitizenRequest.class);
            requestQuery.setParameter("departmentId", dept.getDepartmentId());
            requestQuery.setParameter("requestDate", LocalDate.now().atStartOfDay());
            requestQuery.setMaxResults(1);
            return requestQuery.getSingleResultOrNull();
        }
        catch(Exception e) {
            System.err.println("Đã có lỗi xảy ra khi lấy yêu cầu gần nhất: " + e.getMessage());
            return null;
        }
    }

    public static List<CitizenRequest> getAllRequestsInQueue(UUID deptId) {
        try(Session session = sessionFactory.openSession()) {
            Query<CitizenRequest> requestListQuery = session.createQuery("FROM CitizenRequest WHERE departmentId = :departmentId " +
                            "AND requestDate >= :requestDate AND processStatus = 0" +
                            "ORDER BY requestDate ASC",
                    CitizenRequest.class);
            requestListQuery.setParameter("departmentId", deptId);
            requestListQuery.setParameter("requestDate", LocalDate.now().atStartOfDay());
            return requestListQuery.list();
        }
        catch(Exception e) {
            System.err.println("Đã có lỗi xảy ra khi lấy yêu cầu gần nhất: " + e.getMessage());
            return null;
        }
    }

    public static CitizenRequest getCitizenRequest(UUID requestId) {
        try(Session session = sessionFactory.openSession()) {
            Query<CitizenRequest> requestQuery = session.createQuery("FROM CitizenRequest WHERE requestId = :Id", CitizenRequest.class);
            requestQuery.setParameter("Id", requestId);
            return requestQuery.getSingleResultOrNull();
        }
        catch(Exception e) {
            System.err.println("Có lỗi khi lấy thông tin lượt công dân đến làm việc: " + e.getMessage());
            return null;
        }
    }

    public static void setRequestProcessStatus(UUID requestId, int status) {
        Transaction tx = null;
        try(Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Query<CitizenRequest> requestQuery = session.createQuery("FROM CitizenRequest WHERE requestId = :Id", CitizenRequest.class);
            requestQuery.setParameter("Id", requestId);
            CitizenRequest request = requestQuery.getSingleResultOrNull();
            if(request != null) {
                request.setProcessStatus(status);
                session.merge(request);
                tx.commit();
            }
            else {
                throw new SQLDataException();
            }
        }
        catch(Exception e) {
            if(tx != null) tx.rollback();
            System.err.println("Đã có lỗi xảy ra khi xử lý yêu cầu: " + e.getMessage());
        }
    }

    public static List<CitizenRequest> getAllRequestsFromDept(UUID departmentId) {
        List<CitizenRequest> requestList = null;
        try(Session session = sessionFactory.openSession()) {
            Query<CitizenRequest> requestQuery = session.createQuery("FROM CitizenRequest WHERE departmentId = :Id ORDER BY requestDate DESC", CitizenRequest.class);
            requestQuery.setParameter("Id", departmentId);
            requestList = requestQuery.list();
        }
        catch(Exception e) {
            System.err.println("Đã có lỗi xảy ra khi đọc danh sách lượt công dân đến làm việc: " + e.getMessage());
        }
        return requestList;
    }

    public static int getTicketNumber(String fullName, String nationalId, LocalDateTime requestDate, UUID departmentId) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                CitizenRequest newRequest = new CitizenRequest(fullName, nationalId, requestDate, departmentId);
                newRequest.setRequestNumber(getNewTicketNumber(newRequest.getDepartmentId(), session));
                if(newRequest.getRequestNumber() != -1) {
                    session.persist(newRequest);
                }
                tx.commit();
                return newRequest.getRequestNumber();
            }
            catch(Exception e) {
                if(tx != null) tx.rollback();
                e.printStackTrace();
                return -1;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getNewTicketNumber(UUID departmentID, Session session) {
        int result = -1;
        try {
            Query<Department> departmentQuery = session.createQuery("FROM Department WHERE departmentId = :departmentId", Department.class);
            departmentQuery.setParameter("departmentId", departmentID);
            List<Department> department = departmentQuery.list();
            Department dept = department.get(0);
            Query<LocalDateTime> lastRequestDateQuery = session.createQuery("SELECT requestDate FROM CitizenRequest WHERE departmentId = :departmentId ORDER BY requestDate DESC");
            lastRequestDateQuery.setParameter("departmentId", departmentID);
            List<LocalDateTime> lastRequestDate = lastRequestDateQuery.list();
            if(!lastRequestDate.isEmpty() && !lastRequestDate.get(0).format(DateTimeFormatter.ISO_DATE).equals(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))) {
                dept.setNumOfProcessedRequest(0);
                result = 0;
            }
            if(dept.getNumOfProcessedRequest() < dept.getMaxConcurrentRequestInDay()) {
                result = dept.getNumOfProcessedRequest() + 1;
                dept.setNumOfProcessedRequest(result);
                session.merge(dept);
            }
            else return -1;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Account getAccount(String usrName) {
        Account acc = null;
        try(Session session = sessionFactory.openSession()) {
            Query<Account> accountQuery = session.createQuery("FROM Account WHERE userName = :userName", Account.class);
            accountQuery.setParameter("userName", usrName);
            List<Account> accountList = accountQuery.list();
            if(!accountList.isEmpty()) {
                acc = accountList.get(0);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return acc;
    }

    public static List<Account> getAccounts() {
        List<Account> accountList = null;
        try(Session session = sessionFactory.openSession()) {
            Query<Account> accountQuery = session.createQuery("FROM Account", Account.class);
            accountList = accountQuery.list();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }

    public static boolean changePassword(String userName, String newPwd) {
        Transaction tx = null;
        Account currentAcc = getAccount(userName);
        try(Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            if(currentAcc != null) {
                currentAcc.setHashedPwd(PwdHashHelper.hashPwd(newPwd));
                session.merge(currentAcc);
                tx.commit();
                return true;
            }
            else {
                tx.commit();
                return false;
            }
        }
        catch(Exception e) {
            if(tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setLoginStatus(Account currentAccount, boolean status) {
        boolean isCompleted = false;
        Transaction tx = null;
        try(Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            if(currentAccount != null) {
                currentAccount.setLoginStatus(status);
                session.merge(currentAccount);
                isCompleted = true;
            }
            tx.commit();
        }
        catch(Exception e) {
            if(tx != null) tx.rollback();
            e.printStackTrace();
        }
        return isCompleted;
    }

    public static boolean createAccount(String usrName, String hashedPwd, int role) {
        boolean isCompleted = false;
        Transaction tx = null;
        try(Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Account newAccount = new Account(usrName, hashedPwd, role);
            session.persist(newAccount);
            tx.commit();
            isCompleted = true;
        }
        catch(Exception e) {
            if(tx != null) tx.rollback();
            e.printStackTrace();
        }
        return isCompleted;
    }

    public static boolean deleteAccount(String usrName) {
        boolean isCompleted = false;
        Transaction tx = null;
        try(Session session = sessionFactory.openSession()) {
            List<Account> lacc = getAccounts();
            tx = session.beginTransaction();
            Account currentAccount = getAccount(usrName);
            if(currentAccount != null) {
                if(!currentAccount.getLoginStatus() && lacc.size() > 1) {
                    session.remove(currentAccount);
                    tx.commit();
                    isCompleted = true;
                }
            }
        }
        catch(Exception e) {
            if(tx != null) tx.rollback();
            e.printStackTrace();
        }
        return isCompleted;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("Đã đóng kết nối Database.");
        }
    }
}
