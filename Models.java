package placement;
import java.util.*;

class Student {

    String id;

    String name;

    String email;

    String password;

    String phone;

    String college;

    String department;

    double cgpa;

    List<String> skills;

    String github;

    String linkedin;

    String resume;

    String bio;

    boolean verified;

    List<Application> applications;

    public Student(
            String id,
            String name,
            String email,
            String password
    ) {

        this.id = id;

        this.name = name;

        this.email = email;

        this.password = password;

        this.phone = "";

        this.college = "";

        this.department = "";

        this.cgpa = 0;

        this.skills = new ArrayList<>();

        this.github = "";

        this.linkedin = "";

        this.resume = "";

        this.bio = "";

        this.verified = false;

        this.applications = new ArrayList<>();
    }
}

class Company {

    String id;

    String name;

    String email;

    String password;

    String website;

    String location;

    String hrName;

    String contact;

    String industry;

    int employees;

    String description;

    boolean verified;

    List<Job> jobs;

    List<Student> shortlisted;

    public Company(
            String id,
            String name,
            String email,
            String password
    ) {

        this.id = id;

        this.name = name;

        this.email = email;

        this.password = password;

        this.website = "";

        this.location = "";

        this.hrName = "";

        this.contact = "";

        this.industry = "";

        this.employees = 0;

        this.description = "";

        this.verified = false;

        this.jobs = new ArrayList<>();

        this.shortlisted = new ArrayList<>();
    }
}

class Job {

    String id;

    String title;

    String type;

    String company;

    String companyEmail;

    double cgpa;

    int slots;

    List<String> skills;

    String description;

    List<Application> applications;

    public Job(
            String id,
            String title,
            String type,
            String company,
            String companyEmail,
            double cgpa,
            int slots,
            List<String> skills,
            String description
    ) {

        this.id = id;

        this.title = title;

        this.type = type;

        this.company = company;

        this.companyEmail = companyEmail;

        this.cgpa = cgpa;

        this.slots = slots;

        this.skills = skills;

        this.description = description;

        this.applications = new ArrayList<>();
    }
}

class Application {

    String studentEmail;

    String company;

    String role;

    String status;

    int match;

    String date;

    public Application(
            String studentEmail,
            String company,
            String role,
            String status,
            int match,
            String date
    ) {

        this.studentEmail = studentEmail;

        this.company = company;

        this.role = role;

        this.status = status;

        this.match = match;

        this.date = date;
    }
}

class Recommendation {

    String jobId;

    String title;

    String company;

    int match;

    int eligibility;

    String description;

    public Recommendation(
            String jobId,
            String title,
            String company,
            int match,
            int eligibility,
            String description
    ) {

        this.jobId = jobId;

        this.title = title;

        this.company = company;

        this.match = match;

        this.eligibility = eligibility;

        this.description = description;
    }
}

class SkillGap {

    String company;

    List<String> missingSkills;

    int score;

    public SkillGap(
            String company,
            List<String> missingSkills,
            int score
    ) {

        this.company = company;

        this.missingSkills = missingSkills;

        this.score = score;
    }
}

class OTPData {

    String email;

    String otp;

    long createdTime;

    public OTPData(
            String email,
            String otp
    ) {

        this.email = email;

        this.otp = otp;

        this.createdTime =
                System.currentTimeMillis();
    }
}