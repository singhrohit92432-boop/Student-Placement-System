package placement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;

public class Main {

    static Gson gson = new Gson();

    public static void main(String[] args) {

        port(8080);

        enableCORS();
        Database.connect();

        options("/*", (request, response) -> {

            String headers = request.headers("Access-Control-Request-Headers");

            if (headers != null) {
                response.header("Access-Control-Allow-Headers", headers);
            }

            String method = request.headers("Access-Control-Request-Method");

            if (method != null) {
                response.header("Access-Control-Allow-Methods", method);
            }

            return "OK";
        });

        get("/", (req, res) -> {
            res.type("application/json");
            return gson.toJson(Map.of("message", "Placement Backend Running"));
        });

        /* ========================= */
        /* STUDENT REGISTER */
        /* ========================= */

        post("/student/register", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String name = data.get("name").toString();
                String email = data.get("email").toString();
                String password = data.get("password").toString();
                double cgpa = Double.parseDouble(data.get("cgpa").toString());

                if (!isValidEmail(email)) {
                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Invalid Email"));
                }

                if (!isValidPassword(password)) {
                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Password must contain letters, numbers and minimum 6 characters"));
                }

                Connection con = Database.connect();

                PreparedStatement check = con.prepareStatement(
                        "SELECT * FROM students WHERE email=?");

                check.setString(1, email);

                ResultSet rs = check.executeQuery();

                if (rs.next()) {

                    con.close();

                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Email already exists"));
                }

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO students VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, name);
                ps.setString(3, email);
                ps.setString(4, password);
                ps.setString(5, "");
                ps.setString(6, "");
                ps.setString(7, "");
                ps.setDouble(8, cgpa);
                ps.setString(9, "");
                ps.setString(10, "");
                ps.setString(11, "");
                ps.setString(12, "");
                ps.setString(13, "");
                ps.setBoolean(14, false);

                ps.executeUpdate();

                con.close();

                return gson.toJson(Map.of(
                        "success", true,
                        "message", "Student Registered Successfully"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Registration Failed"));
            }
        });

        /* ========================= */
        /* STUDENT LOGIN */
        /* ========================= */

        post("/student/login", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String email = data.get("email").toString();
                String password = data.get("password").toString();

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM students WHERE email=? AND password=?");

                ps.setString(1, email);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    Student student = makeStudentFromResultSet(rs);

                    con.close();

                    return gson.toJson(Map.of(
                            "success", true,
                            "student", student));
                }

                con.close();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Invalid Credentials"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Login Failed"));
            }
        });

        /* ========================= */
        /* SAVE STUDENT PROFILE */
        /* ========================= */

        post("/student/profile", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String name = getString(data, "name");
                String email = getString(data, "email");
                String phone = getString(data, "phone");
                String college = getString(data, "college");
                String department = getString(data, "department");
                double cgpa = getDouble(data, "cgpa");
                String github = getString(data, "github");
                String linkedin = getString(data, "linkedin");
                String resume = getString(data, "resume");
                String bio = getString(data, "bio");

                List<String> skillsList = getStringList(data, "skills");

                String skills = String.join(",", skillsList);

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE students SET name=?, phone=?, college=?, department=?, cgpa=?, skills=?, github=?, linkedin=?, resume=?, bio=? WHERE email=?");

                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, college);
                ps.setString(4, department);
                ps.setDouble(5, cgpa);
                ps.setString(6, skills);
                ps.setString(7, github);
                ps.setString(8, linkedin);
                ps.setString(9, resume);
                ps.setString(10, bio);
                ps.setString(11, email);

                int updated = ps.executeUpdate();

                con.close();

                if (updated > 0) {
                    return gson.toJson(Map.of(
                            "success", true,
                            "message", "Student Profile Saved Successfully"));
                }

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Student Not Found"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Student Profile Save Failed"));
            }
        });

        /* ========================= */
        /* COMPANY REGISTER */
        /* ========================= */

        post("/company/register", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String name = data.get("name").toString();
                String email = data.get("email").toString();
                String password = data.get("password").toString();

                if (!isValidEmail(email)) {
                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Invalid Email"));
                }

                if (!isValidPassword(password)) {
                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Password must contain letters, numbers and minimum 6 characters"));
                }

                Connection con = Database.connect();

                PreparedStatement check = con.prepareStatement(
                        "SELECT * FROM companies WHERE email=?");

                check.setString(1, email);

                ResultSet rs = check.executeQuery();

                if (rs.next()) {

                    con.close();

                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Email already exists"));
                }

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO companies VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, name);
                ps.setString(3, email);
                ps.setString(4, password);
                ps.setString(5, "");
                ps.setString(6, "");
                ps.setString(7, "");
                ps.setString(8, "");
                ps.setString(9, "");
                ps.setInt(10, 0);
                ps.setString(11, "");
                ps.setBoolean(12, false);

                ps.executeUpdate();

                con.close();

                return gson.toJson(Map.of(
                        "success", true,
                        "message", "Company Registered Successfully"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Company Registration Failed"));
            }
        });

        /* ========================= */
        /* COMPANY LOGIN */
        /* ========================= */

        post("/company/login", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String email = data.get("email").toString();
                String password = data.get("password").toString();

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM companies WHERE email=? AND password=?");

                ps.setString(1, email);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    Company company = makeCompanyFromResultSet(rs);

                    con.close();

                    return gson.toJson(Map.of(
                            "success", true,
                            "company", company));
                }

                con.close();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Invalid Credentials"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Login Failed"));
            }
        });

        /* ========================= */
        /* SAVE COMPANY PROFILE */
        /* ========================= */

        post("/company/profile", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String name = getString(data, "name");
                String email = getString(data, "email");
                String website = getString(data, "website");
                String location = getString(data, "location");
                String hrName = getString(data, "hrName");
                String contact = getString(data, "contact");
                String industry = getString(data, "industry");
                int employees = (int) getDouble(data, "employees");
                String description = getString(data, "description");

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE companies SET name=?, website=?, location=?, hrName=?, contact=?, industry=?, employees=?, description=? WHERE email=?");

                ps.setString(1, name);
                ps.setString(2, website);
                ps.setString(3, location);
                ps.setString(4, hrName);
                ps.setString(5, contact);
                ps.setString(6, industry);
                ps.setInt(7, employees);
                ps.setString(8, description);
                ps.setString(9, email);

                int updated = ps.executeUpdate();

                con.close();

                if (updated > 0) {
                    return gson.toJson(Map.of(
                            "success", true,
                            "message", "Company Profile Saved Successfully"));
                }

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Company Not Found"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Company Profile Save Failed"));
            }
        });

        /* ========================= */
        /* GET ALL JOBS */
        /* ========================= */

        get("/jobs", (req, res) -> {

            res.type("application/json");

            try {

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM jobs");

                ResultSet rs = ps.executeQuery();

                List<Job> jobs = new ArrayList<>();

                while (rs.next()) {
                    jobs.add(makeJobFromResultSet(rs));
                }

                con.close();

                return gson.toJson(jobs);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* ADD JOB */
        /* ========================= */

        post("/jobs/add", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO jobs VALUES(?,?,?,?,?,?,?,?,?)");

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, getString(data, "title"));
                ps.setString(3, getString(data, "type"));
                ps.setString(4, getString(data, "companyName"));
                ps.setString(5, getString(data, "companyEmail"));
                ps.setDouble(6, getDouble(data, "cgpa"));
                ps.setInt(7, (int) getDouble(data, "slots"));

                List<String> skills = getStringList(data, "skills");

                ps.setString(8, String.join(",", skills));
                ps.setString(9, getString(data, "description"));

                ps.executeUpdate();

                con.close();

                return gson.toJson(Map.of(
                        "success", true,
                        "message", "Job Added Successfully"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Job Add Failed"));
            }
        });

        /* ========================= */
        /* COMPANY JOBS */
        /* ========================= */

        get("/company/jobs/:email", (req, res) -> {

            res.type("application/json");

            try {

                String companyEmail = req.params(":email");

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM jobs WHERE companyEmail=?");

                ps.setString(1, companyEmail);

                ResultSet rs = ps.executeQuery();

                List<Job> jobs = new ArrayList<>();

                while (rs.next()) {
                    jobs.add(makeJobFromResultSet(rs));
                }

                con.close();

                return gson.toJson(jobs);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* DELETE JOB */
        /* ========================= */

        delete("/jobs/delete/:id", (req, res) -> {

            res.type("application/json");

            try {

                String jobId = req.params(":id");

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM jobs WHERE id=?");

                ps.setString(1, jobId);

                int deleted = ps.executeUpdate();

                con.close();

                if (deleted > 0) {
                    return gson.toJson(Map.of(
                            "success", true,
                            "message", "Job Deleted Successfully"));
                }

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Job Not Found"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Job Delete Failed"));
            }
        });

        /* ========================= */
        /* APPLY JOB */
        /* ========================= */

        post("/apply", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                Connection con = Database.connect();

                PreparedStatement check = con.prepareStatement(
                        "SELECT * FROM applications WHERE studentEmail=? AND company=? AND role=?");

                check.setString(1, getString(data, "studentEmail"));
                check.setString(2, getString(data, "company"));
                check.setString(3, getString(data, "role"));

                ResultSet checkRs = check.executeQuery();

                if (checkRs.next()) {

                    con.close();

                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "You already applied for this job"));
                }

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO applications(studentEmail,company,role,status,matchScore,appliedDate) VALUES(?,?,?,?,?,?)");

                ps.setString(1, getString(data, "studentEmail"));
                ps.setString(2, getString(data, "company"));
                ps.setString(3, getString(data, "role"));
                ps.setString(4, "Applied");
                ps.setInt(5, (int) getDouble(data, "match"));
                ps.setString(6, new Date().toString());

                ps.executeUpdate();

                con.close();

                return gson.toJson(Map.of(
                        "success", true,
                        "message", "Applied Successfully"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Application Failed"));
            }
        });

        /* ========================= */
        /* STUDENT APPLICATIONS */
        /* ========================= */

        get("/applications/:email", (req, res) -> {

            res.type("application/json");

            try {

                String email = req.params(":email");

                Connection con = Database.connect();

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM applications WHERE studentEmail=?");

                ps.setString(1, email);

                ResultSet rs = ps.executeQuery();

                List<Application> applications = new ArrayList<>();

                while (rs.next()) {

                    Application app = new Application(
                            rs.getString("studentEmail"),
                            rs.getString("company"),
                            rs.getString("role"),
                            rs.getString("status"),
                            rs.getInt("matchScore"),
                            rs.getString("appliedDate"));

                    applications.add(app);
                }

                con.close();

                return gson.toJson(applications);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* RECOMMENDATIONS */
        /* ========================= */

        get("/recommendations/:email", (req, res) -> {

            res.type("application/json");

            try {

                String email = req.params(":email");

                Connection con = Database.connect();

                Student student = getStudentFromDatabase(con, email);

                if (student == null) {

                    con.close();

                    return gson.toJson(new ArrayList<>());
                }

                List<Job> jobs = getAllJobsFromDatabase(con);

                List<Recommendation> recommendations = Algorithms.generateRecommendations(student, jobs);

                con.close();

                return gson.toJson(recommendations);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* SKILL GAP */
        /* ========================= */

        get("/skillgap/:email", (req, res) -> {

            res.type("application/json");

            try {

                String email = req.params(":email");

                Connection con = Database.connect();

                Student student = getStudentFromDatabase(con, email);

                if (student == null) {

                    con.close();

                    return gson.toJson(new ArrayList<>());
                }

                List<Job> jobs = getAllJobsFromDatabase(con);

                List<SkillGap> gaps = Algorithms.analyzeSkillGap(student, jobs);

                con.close();

                return gson.toJson(gaps);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* COMPANY APPLICANTS */
        /* ========================= */

        get("/company/applicants/:email", (req, res) -> {

            res.type("application/json");

            try {

                String companyEmail = req.params(":email");

                Connection con = Database.connect();

                Company company = getCompanyFromDatabase(con, companyEmail);

                if (company == null) {

                    con.close();

                    return gson.toJson(new ArrayList<>());
                }

                PreparedStatement ps = con.prepareStatement(
                        "SELECT s.*, a.matchScore FROM students s INNER JOIN applications a ON s.email=a.studentEmail WHERE a.company=? AND a.status <> ?");

                ps.setString(1, company.name);
                ps.setString(2, "Rejected");

                ResultSet rs = ps.executeQuery();

                List<Student> applicants = new ArrayList<>();

                while (rs.next()) {

                    Student student = makeStudentFromResultSet(rs);

                    student.applications = new ArrayList<>();

                    // Store match score using a temporary application object
                    Application app = new Application(
                            student.email,
                            company.name,
                            "",
                            "",
                            rs.getInt("matchScore"),
                            "");

                    student.applications.add(app);

                    applicants.add(student);
                }

                con.close();

                return gson.toJson(applicants);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* SHORTLIST STUDENT */
        /* ========================= */

        post("/shortlist", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String companyEmail = getString(data, "companyEmail");
                String studentEmail = getString(data, "studentEmail");

                Connection con = Database.connect();

                Company company = getCompanyFromDatabase(con, companyEmail);

                if (company == null) {

                    con.close();

                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Company Not Found"));
                }

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE applications SET status=? WHERE company=? AND studentEmail=?");

                ps.setString(1, "Shortlisted");
                ps.setString(2, company.name);
                ps.setString(3, studentEmail);

                int updated = ps.executeUpdate();

                con.close();

                if (updated > 0) {
                    return gson.toJson(Map.of(
                            "success", true,
                            "message", "Student Shortlisted Successfully"));
                }

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Application Not Found"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Shortlist Failed"));
            }
        });

        /* ========================= */
        /* REJECT STUDENT */
        /* ========================= */

        post("/reject", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String companyEmail = getString(data, "companyEmail");
                String studentEmail = getString(data, "studentEmail");

                Connection con = Database.connect();

                Company company = getCompanyFromDatabase(con, companyEmail);

                if (company == null) {

                    con.close();

                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Company Not Found"));
                }

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE applications SET status=? WHERE company=? AND studentEmail=?");

                ps.setString(1, "Rejected");
                ps.setString(2, company.name);
                ps.setString(3, studentEmail);

                int updated = ps.executeUpdate();

                con.close();

                if (updated > 0) {
                    return gson.toJson(Map.of(
                            "success", true,
                            "message", "Student Rejected Successfully"));
                }

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Application Not Found"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Reject Failed"));
            }
        });

        /* ========================= */
        /* COMPANY SHORTLISTED */
        /* ========================= */

        get("/company/shortlisted/:email", (req, res) -> {

            res.type("application/json");

            try {

                String companyEmail = req.params(":email");

                Connection con = Database.connect();

                Company company = getCompanyFromDatabase(con, companyEmail);

                if (company == null) {

                    con.close();

                    return gson.toJson(new ArrayList<>());
                }

                PreparedStatement ps = con.prepareStatement(
                        "SELECT s.*, a.matchScore FROM students s INNER JOIN applications a ON s.email=a.studentEmail WHERE a.company=? AND a.status=?");

                ps.setString(1, company.name);
                ps.setString(2, "Shortlisted");

                ResultSet rs = ps.executeQuery();

                List<Student> students = new ArrayList<>();

                while (rs.next()) {

                    Student student = makeStudentFromResultSet(rs);

                    Application app = new Application(
                            student.email,
                            company.name,
                            "",
                            "Shortlisted",
                            rs.getInt("matchScore"),
                            "");

                    student.applications.add(app);

                    students.add(student);
                }

                con.close();

                return gson.toJson(students);

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(new ArrayList<>());
            }
        });

        /* ========================= */
        /* SELECT STUDENT */
        /* ========================= */

        post("/select", (req, res) -> {

            res.type("application/json");

            try {

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String companyEmail = getString(data, "companyEmail");
                String studentEmail = getString(data, "studentEmail");

                Connection con = Database.connect();

                Company company = getCompanyFromDatabase(con, companyEmail);

                if (company == null) {

                    con.close();

                    return gson.toJson(Map.of(
                            "success", false,
                            "message", "Company Not Found"));
                }

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE applications SET status=? WHERE company=? AND studentEmail=?");

                ps.setString(1, "Selected");
                ps.setString(2, company.name);
                ps.setString(3, studentEmail);

                int updated = ps.executeUpdate();

                con.close();

                if (updated > 0) {
                    return gson.toJson(Map.of(
                            "success", true,
                            "message", "Student Selected Successfully"));
                }

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Application Not Found"));

            } catch (Exception e) {

                e.printStackTrace();

                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Selection Failed"));
            }
        });

    }

    /* ========================= */
    /* CORS */
    /* ========================= */

    private static void enableCORS() {

        before((request, response) -> {

            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.header("Access-Control-Allow-Headers", "*");

        });
    }

    /* ========================= */
    /* HELPERS */
    /* ========================= */

    private static String getString(Map<String, Object> data, String key) {

        if (data.get(key) == null) {
            return "";
        }

        return data.get(key).toString();
    }

    private static double getDouble(Map<String, Object> data, String key) {

        try {

            if (data.get(key) == null) {
                return 0;
            }

            return Double.parseDouble(data.get(key).toString());

        } catch (Exception e) {
            return 0;
        }
    }

    private static List<String> getStringList(Map<String, Object> data, String key) {

        List<String> result = new ArrayList<>();

        Object value = data.get(key);

        if (value == null) {
            return result;
        }

        if (value instanceof List<?>) {

            List<?> rawList = (List<?>) value;

            for (Object item : rawList) {

                if (item != null && !item.toString().trim().isEmpty()) {
                    result.add(item.toString().trim());
                }
            }

            return result;
        }

        String text = value.toString();

        if (text.trim().isEmpty()) {
            return result;
        }

        String[] parts = text.split(",");

        for (String part : parts) {

            if (!part.trim().isEmpty()) {
                result.add(part.trim());
            }
        }

        return result;
    }

    private static Student makeStudentFromResultSet(ResultSet rs) throws Exception {

        Student student = new Student(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"));

        student.phone = rs.getString("phone");
        student.college = rs.getString("college");
        student.department = rs.getString("department");
        student.cgpa = rs.getDouble("cgpa");
        student.github = rs.getString("github");
        student.linkedin = rs.getString("linkedin");
        student.resume = rs.getString("resume");
        student.bio = rs.getString("bio");
        student.verified = rs.getBoolean("verified");

        String skills = rs.getString("skills");

        if (skills != null && !skills.isEmpty()) {
            student.skills = Arrays.asList(skills.split(","));
        }

        return student;
    }

    private static Company makeCompanyFromResultSet(ResultSet rs) throws Exception {

        Company company = new Company(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"));

        company.website = rs.getString("website");
        company.location = rs.getString("location");
        company.hrName = rs.getString("hrName");
        company.contact = rs.getString("contact");
        company.industry = rs.getString("industry");
        company.employees = rs.getInt("employees");
        company.description = rs.getString("description");
        company.verified = rs.getBoolean("verified");

        return company;
    }

    private static Job makeJobFromResultSet(ResultSet rs) throws Exception {

        List<String> skills = new ArrayList<>();

        String skillsText = rs.getString("skills");

        if (skillsText != null && !skillsText.isEmpty()) {
            skills = Arrays.asList(skillsText.split(","));
        }

        return new Job(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("type"),
                rs.getString("company"),
                rs.getString("companyEmail"),
                rs.getDouble("cgpa"),
                rs.getInt("slots"),
                skills,
                rs.getString("description"));
    }

    private static Student getStudentFromDatabase(Connection con, String email) throws Exception {

        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM students WHERE email=?");

        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return makeStudentFromResultSet(rs);
        }

        return null;
    }

    private static Company getCompanyFromDatabase(Connection con, String email) throws Exception {

        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM companies WHERE email=?");

        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return makeCompanyFromResultSet(rs);
        }

        return null;
    }

    private static List<Job> getAllJobsFromDatabase(Connection con) throws Exception {

        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM jobs");

        ResultSet rs = ps.executeQuery();

        List<Job> jobs = new ArrayList<>();

        while (rs.next()) {
            jobs.add(makeJobFromResultSet(rs));
        }

        return jobs;
    }

    private static boolean isValidEmail(String email) {

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private static boolean isValidPassword(String password) {

        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {

            if (Character.isLetter(c)) {
                hasLetter = true;
            }

            if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        return hasLetter && hasNumber;
    }
}