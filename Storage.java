package placement;
import java.util.*;

public class Storage {

    public static List<Student> students =
            new ArrayList<>();

    public static List<Company> companies =
            new ArrayList<>();

    public static List<Job> jobs =
            new ArrayList<>();

    public static List<Application> applications =
            new ArrayList<>();

    public static List<OTPData> otpList =
            new ArrayList<>();

    /* ========================= */
    /* ADD STUDENT */
    /* ========================= */

    public static void addStudent(
            Student student
    ) {

        students.add(student);

    }

    /* ========================= */
    /* ADD COMPANY */
    /* ========================= */

    public static void addCompany(
            Company company
    ) {

        companies.add(company);

    }

    /* ========================= */
    /* ADD JOB */
    /* ========================= */

    public static void addJob(
            Job job
    ) {

        jobs.add(job);

        for(Company c : companies){

            if(
                    c.email.equals(
                            job.companyEmail
                    )
            ){

                c.jobs.add(job);

                break;
            }
        }

    }

    /* ========================= */
    /* ADD APPLICATION */
    /* ========================= */

    public static void addApplication(
            Application application
    ) {

        applications.add(application);

        Student student =
                getStudentByEmail(
                        application.studentEmail
                );

        if(student != null){

            student.applications.add(
                    application
            );

        }

    }

    /* ========================= */
    /* GET STUDENT */
    /* ========================= */

    public static Student getStudentByEmail(
            String email
    ) {

        for(Student s : students){

            if(
                    s.email.equalsIgnoreCase(
                            email
                    )
            ){

                return s;

            }

        }

        return null;

    }

    /* ========================= */
    /* GET COMPANY */
    /* ========================= */

    public static Company getCompanyByEmail(
            String email
    ) {

        for(Company c : companies){

            if(
                    c.email.equalsIgnoreCase(
                            email
                    )
            ){

                return c;

            }

        }

        return null;

    }

    /* ========================= */
    /* GET JOB */
    /* ========================= */

    public static Job getJobById(
            String id
    ) {

        for(Job j : jobs){

            if(
                    j.id.equals(id)
            ){

                return j;

            }

        }

        return null;

    }

    /* ========================= */
    /* LOGIN STUDENT */
    /* ========================= */

    public static Student loginStudent(
            String email,
            String password
    ) {

        for(Student s : students){

            if(
                    s.email.equalsIgnoreCase(email)
                    &&
                    s.password.equals(password)
            ){

                return s;

            }

        }

        return null;

    }

    /* ========================= */
    /* LOGIN COMPANY */
    /* ========================= */

    public static Company loginCompany(
            String email,
            String password
    ) {

        for(Company c : companies){

            if(
                    c.email.equalsIgnoreCase(email)
                    &&
                    c.password.equals(password)
            ){

                return c;

            }

        }

        return null;

    }

    /* ========================= */
    /* DELETE JOB */
    /* ========================= */

    public static boolean deleteJob(
            String jobId
    ) {

        Job found = null;

        for(Job j : jobs){

            if(
                    j.id.equals(jobId)
            ){

                found = j;

                break;

            }

        }

        if(found != null){

            jobs.remove(found);

            return true;

        }

        return false;

    }

    /* ========================= */
    /* GET APPLICATIONS */
    /* ========================= */

    public static List<Application>
    getApplicationsByStudent(
            String email
    ){

        List<Application> result =
                new ArrayList<>();

        for(Application app : applications){

            if(
                    app.studentEmail
                    .equalsIgnoreCase(email)
            ){

                result.add(app);

            }

        }

        return result;

    }

    /* ========================= */
    /* GET COMPANY JOBS */
    /* ========================= */

    public static List<Job>
    getJobsByCompany(
            String companyEmail
    ){

        List<Job> result =
                new ArrayList<>();

        for(Job j : jobs){

            if(
                    j.companyEmail
                    .equalsIgnoreCase(companyEmail)
            ){

                result.add(j);

            }

        }

        return result;

    }

    /* ========================= */
    /* SAVE OTP */
    /* ========================= */

    public static void saveOTP(
            String email,
            String otp
    ){

        otpList.add(
                new OTPData(
                        email,
                        otp
                )
        );

    }

    /* ========================= */
    /* VERIFY OTP */
    /* ========================= */

    public static boolean verifyOTP(
            String email,
            String otp
    ){

        for(OTPData data : otpList){

            if(
                    data.email.equals(email)
                    &&
                    data.otp.equals(otp)
            ){

                return true;

            }

        }

        return false;

    }

    /* ========================= */
    /* EMAIL EXISTS */
    /* ========================= */

    public static boolean emailExists(
            String email
    ){

        for(Student s : students){

            if(
                    s.email.equalsIgnoreCase(email)
            ){

                return true;

            }

        }

        for(Company c : companies){

            if(
                    c.email.equalsIgnoreCase(email)
            ){

                return true;

            }

        }

        return false;

    }

    /* ========================= */
    /* PRINT DATA */
    /* ========================= */

    public static void printAllData(){

        System.out.println(
                "===== STUDENTS ====="
        );

        for(Student s : students){

            System.out.println(
                    s.name +
                    " | " +
                    s.email
            );

        }

        System.out.println(
                "===== COMPANIES ====="
        );

        for(Company c : companies){

            System.out.println(
                    c.name +
                    " | " +
                    c.email
            );

        }

        System.out.println(
                "===== JOBS ====="
        );

        for(Job j : jobs){

            System.out.println(
                    j.title +
                    " | " +
                    j.company
            );

        }

    }

}