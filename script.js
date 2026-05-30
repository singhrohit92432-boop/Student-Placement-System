/* ========================================================= */
/* STUDENT PLACEMENT SYSTEM - FINAL FRONTEND CONTROLLER */
/* ========================================================= */
/* Backend base URL */
const API = "https://student-placement-backend.onrender.com"; 

/* ========================================================= */
/* GLOBAL STATE */
/* ========================================================= */

let currentStudent = null;
let currentCompany = null;

let originalJobs = [];
let displayedJobs = [];

let recommendations = [];
let applications = [];
let skillGapData = [];

let companyJobs = [];
let companyApplicants = [];
let shortlistedStudents = [];

/* ========================================================= */
/* TOAST SYSTEM - NO ALERTS */
/* ========================================================= */

function showToast(message, type = "success") {
    const toastBox = document.getElementById("toastBox");

    if (!toastBox) {
        console.log(message);
        return;
    }

    const toast = document.createElement("div");

    toast.className = "toast " + type;
    toast.innerText = message;

    toastBox.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(30px)";
    }, 2600);

    setTimeout(() => {
        toast.remove();
    }, 3200);
}

/* ========================================================= */
/* SAFE HELPERS */
/* ========================================================= */

function safeNumber(value, fallback = 0) {
    const number = parseFloat(value);

    if (isNaN(number)) {
        return fallback;
    }

    return number;
}

function normalizeSkills(skills) {
    if (!skills) {
        return [];
    }

    if (Array.isArray(skills)) {
        return skills
            .map(skill => String(skill).trim())
            .filter(skill => skill !== "");
    }

    if (typeof skills === "string") {
        return skills
            .split(",")
            .map(skill => skill.trim())
            .filter(skill => skill !== "");
    }

    return [];
}

function skillsToText(skills) {
    const list = normalizeSkills(skills);

    if (list.length === 0) {
        return "Not specified";
    }

    return list.join(", ");
}

function getValue(id) {
    const element = document.getElementById(id);

    if (!element) {
        return "";
    }

    return element.value.trim();
}

function setValue(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.value = value || "";
    }
}

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.innerText = value;
    }
}

function clearElement(id) {
    const element = document.getElementById(id);

    if (element) {
        element.innerHTML = "";
    }
}

function showElement(id) {
    const element = document.getElementById(id);

    if (element) {
        element.style.display = "block";

        if (id === "profileFormCard" || id === "companyProfileCard") {
            const shell = element.closest(".dashboardShell");
            if (shell) {
                shell.classList.remove("noProfile");
            }
        }
    }
}

function hideElement(id) {
    const element = document.getElementById(id);

    if (element) {
        element.style.display = "none";

        if (id === "profileFormCard" || id === "companyProfileCard") {
            const shell = element.closest(".dashboardShell");
            if (shell) {
                shell.classList.add("noProfile");
            }
        }
    }
}

function resetForm(id) {
    const form = document.getElementById(id);

    if (form) {
        form.reset();
    }
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    return emailRegex.test(email);
}

function isValidPassword(password) {
    return password.length >= 6;
}

/* ========================================================= */
/* SAFE API RESPONSE HANDLER */
/* ========================================================= */

async function safeJsonResponse(response) {
    const text = await response.text();

    if (!text) {
        return {};
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return {
            message: text
        };
    }
}

async function apiGet(endpoint) {
    const response = await fetch(API + endpoint);

    const data = await safeJsonResponse(response);

    return {
        ok: response.ok,
        status: response.status,
        data: data
    };
}

async function apiPost(endpoint, bodyData) {
    const response = await fetch(API + endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(bodyData)
    });

    const data = await safeJsonResponse(response);

    return {
        ok: response.ok,
        status: response.status,
        data: data
    };
}

async function apiDelete(endpoint) {
    const response = await fetch(API + endpoint, {
        method: "DELETE"
    });

    const data = await safeJsonResponse(response);

    return {
        ok: response.ok,
        status: response.status,
        data: data
    };
}

/* ========================================================= */
/* PAGE ROUTING */
/* ========================================================= */

function hideAllPages() {
    document.querySelectorAll(".page").forEach(page => {
        page.classList.remove("activePage");
    });
}

function showPage(pageId) {
    hideAllPages();

    const page = document.getElementById(pageId);

    if (page) {
        page.classList.add("activePage");
    }
}

function goHome() {
    showPage("landingPage");
}

function openStudentAuth() {
    showPage("studentAuthPage");
}

function openCompanyAuth() {
    showPage("companyAuthPage");
}

function openAbout() {
    showPage("aboutPage");
}

/* ========================================================= */
/* LOGIN CHECKS */
/* ========================================================= */

function requireStudent() {
    if (!currentStudent) {
        showToast("Please login first", "warning");
        openStudentAuth();
        return false;
    }

    return true;
}

function requireCompany() {
    if (!currentCompany) {
        showToast("Please login first", "warning");
        openCompanyAuth();
        return false;
    }

    return true;
}

/* ========================================================= */
/* SECTION SWITCHING */
/* ========================================================= */

function showStudentSection(sectionId) {
    document.querySelectorAll(".studentSection").forEach(section => {
        section.classList.remove("activeStudentSection");
    });

    const activeSection = document.getElementById(sectionId);

    if (activeSection) {
        activeSection.classList.add("activeStudentSection");
    }
}

function showCompanySection(sectionId) {
    document.querySelectorAll(".companySection").forEach(section => {
        section.classList.remove("activeCompanySection");
    });

    const activeSection = document.getElementById(sectionId);

    if (activeSection) {
        activeSection.classList.add("activeCompanySection");
    }
}

/* ========================================================= */
/* DASHBOARD LOADERS */
/* ========================================================= */

async function loadStudentDashboard() {
    if (!requireStudent()) {
        return;
    }

    showPage("studentDashboard");

    loadStudentProfile();

    await fetchJobs();
    await fetchRecommendations();
    await fetchApplications();
    await fetchSkillGap();

    showStudentSection("studentHome");
}

async function loadCompanyDashboard() {
    if (!requireCompany()) {
        return;
    }

    showPage("companyDashboard");

    await fetchCompanyJobs();
    await fetchApplicants();
    await fetchShortlisted();

    showCompanySection("companyHome");
}

/* ========================================================= */
/* STUDENT AUTH */
/* ========================================================= */

async function studentLogin() {
    const email = getValue("studentLoginEmail");
    const password = getValue("studentLoginPassword");

    if (email === "" || password === "") {
        showToast("Please fill all login fields", "warning");
        return;
    }

    try {
        const result = await apiPost("/student/login", {
            email: email,
            password: password
        });

        if (!result.ok || !result.data.success) {
            showToast(result.data.message || "Invalid student login", "error");
            return;
        }

        currentStudent = result.data.student;

        if (!currentStudent.skills) {
            currentStudent.skills = [];
        }

        currentStudent.skills = normalizeSkills(currentStudent.skills);

        showToast("Student login successful", "success");

        resetLoginInputs();

        await loadStudentDashboard();

        if (isStudentProfileIncomplete()) {
            showElement("profileFormCard");
            fillStudentProfileForm();
            showStudentSection("studentProfile");
            showToast("Complete your profile for better recommendations", "warning");
        } else {
            hideElement("profileFormCard");
        }

    } catch (error) {
        console.log(error);
        showToast("Backend connection failed", "error");
    }
}

async function studentRegister() {
    const name = getValue("studentRegisterName");
    const email = getValue("studentRegisterEmail");
    const password = getValue("studentRegisterPassword");
    const cgpa = getValue("studentRegisterCgpa");

    if (name === "" || email === "" || password === "" || cgpa === "") {
        showToast("Please fill all registration fields", "warning");
        return;
    }

    if (!isValidEmail(email)) {
        showToast("Enter a valid email", "warning");
        return;
    }

    if (!isValidPassword(password)) {
        showToast("Password must be at least 6 characters", "warning");
        return;
    }

    try {
        const result = await apiPost("/student/register", {
            name: name,
            email: email,
            password: password,
            cgpa: safeNumber(cgpa)
        });

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Student registration failed", "error");
            return;
        }

        resetForm("studentRegisterForm");

        currentStudent = {
            id: "",
            name: name,
            email: email,
            password: password,
            phone: "",
            college: "",
            department: "",
            cgpa: safeNumber(cgpa),
            skills: [],
            github: "",
            linkedin: "",
            resume: "",
            bio: "",
            verified: false
        };

        showToast("Registered successfully. Complete your profile now.", "success");

        await loadStudentDashboard();

        showElement("profileFormCard");
        fillStudentProfileForm();
        showStudentSection("studentProfile");

    } catch (error) {
        console.log(error);
        showToast("Backend connection failed", "error");
    }
}

function resetLoginInputs() {
    setValue("studentLoginEmail", "");
    setValue("studentLoginPassword", "");
    setValue("companyLoginEmail", "");
    setValue("companyLoginPassword", "");
}

/* ========================================================= */
/* COMPANY AUTH */
/* ========================================================= */

async function companyLogin() {
    const email = getValue("companyLoginEmail");
    const password = getValue("companyLoginPassword");

    if (email === "" || password === "") {
        showToast("Please fill all login fields", "warning");
        return;
    }

    try {
        const result = await apiPost("/company/login", {
            email: email,
            password: password
        });

        if (!result.ok || !result.data.success) {
            showToast(result.data.message || "Invalid company login", "error");
            return;
        }

        currentCompany = result.data.company;

        showToast("Company login successful", "success");

        resetLoginInputs();

        await loadCompanyDashboard();

        if (isCompanyProfileIncomplete()) {
            showElement("companyProfileCard");
            fillCompanyProfileForm();
            showCompanySection("companyHome");
            showToast("Complete company profile before posting jobs", "warning");
        } else {
            hideElement("companyProfileCard");
        }

    } catch (error) {
        console.log(error);
        showToast("Backend connection failed", "error");
    }
}

async function companyRegister() {
    const name = getValue("companyRegisterName");
    const email = getValue("companyRegisterEmail");
    const password = getValue("companyRegisterPassword");

    if (name === "" || email === "" || password === "") {
        showToast("Please fill all registration fields", "warning");
        return;
    }

    if (!isValidEmail(email)) {
        showToast("Enter a valid email", "warning");
        return;
    }

    if (!isValidPassword(password)) {
        showToast("Password must be at least 6 characters", "warning");
        return;
    }

    try {
        const result = await apiPost("/company/register", {
            name: name,
            email: email,
            password: password
        });

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Company registration failed", "error");
            return;
        }

        resetForm("companyRegisterForm");

        currentCompany = {
            id: "",
            name: name,
            email: email,
            password: password,
            website: "",
            location: "",
            hrName: "",
            contact: "",
            industry: "",
            employees: 0,
            description: "",
            verified: false
        };

        showToast("Company registered successfully. Complete profile now.", "success");

        await loadCompanyDashboard();

        showElement("companyProfileCard");
        fillCompanyProfileForm();
        showCompanySection("companyHome");

    } catch (error) {
        console.log(error);
        showToast("Backend connection failed", "error");
    }
}
/* ========================================================= */
/* PROFILE CHECKS + FORM FILLING */
/* ========================================================= */

function isStudentProfileIncomplete() {
    if (!currentStudent) {
        return true;
    }

    const skills = normalizeSkills(currentStudent.skills);

    return (
        !currentStudent.phone ||
        !currentStudent.college ||
        !currentStudent.department ||
        skills.length === 0
    );
}

function isCompanyProfileIncomplete() {
    if (!currentCompany) {
        return true;
    }

    return (
        !currentCompany.website ||
        !currentCompany.location ||
        !currentCompany.hrName ||
        !currentCompany.contact ||
        !currentCompany.industry
    );
}

function fillStudentProfileForm() {
    if (!currentStudent) {
        return;
    }

    setValue("studentFullName", currentStudent.name || "");
    setValue("studentProfileEmail", currentStudent.email || "");
    setValue("studentPhone", currentStudent.phone || "");
    setValue("studentCollege", currentStudent.college || "");
    setValue("studentDepartment", currentStudent.department || "");
    setValue("studentCgpaInput", currentStudent.cgpa || "");
    setValue("studentSkillsInput", skillsToText(currentStudent.skills));
    setValue("studentGithub", currentStudent.github || "");
    setValue("studentLinkedin", currentStudent.linkedin || "");
    setValue("studentResume", currentStudent.resume || "");
    setValue("studentBio", currentStudent.bio || "");

    const emailInput = document.getElementById("studentProfileEmail");

    if (emailInput) {
        emailInput.readOnly = true;
    }
}

function fillCompanyProfileForm() {
    if (!currentCompany) {
        return;
    }

    setValue("companyNameInput", currentCompany.name || "");
    setValue("companyEmailInput", currentCompany.email || "");
    setValue("companyWebsite", currentCompany.website || "");
    setValue("companyLocation", currentCompany.location || "");
    setValue("companyHR", currentCompany.hrName || currentCompany.hr || "");
    setValue("companyContact", currentCompany.contact || "");
    setValue("companyIndustry", currentCompany.industry || "");
    setValue("companyEmployees", currentCompany.employees || "");
    setValue("companyDescription", currentCompany.description || "");

    const emailInput = document.getElementById("companyEmailInput");

    if (emailInput) {
        emailInput.readOnly = true;
    }
}

/* ========================================================= */
/* LOAD STUDENT PROFILE VIEW */
/* ========================================================= */

function loadStudentProfile() {
    if (!currentStudent) {
        return;
    }

    setText("studentName", currentStudent.name || "Student Name");
    setText("studentEmail", currentStudent.email || "student@email.com");
    setText("studentCgpa", currentStudent.cgpa || 0);

    const skillsBox = document.getElementById("studentSkills");

    if (skillsBox) {
        skillsBox.innerHTML = "";

        const skills = normalizeSkills(currentStudent.skills);

        if (skills.length === 0) {
            skillsBox.innerHTML = `<span>No skills added</span>`;
        } else {
            skills.forEach(skill => {
                skillsBox.innerHTML += `<span>${skill}</span>`;
            });
        }
    }

    fillStudentProfileForm();
}

/* ========================================================= */
/* SAVE STUDENT PROFILE */
/* ========================================================= */

async function saveStudentProfile() {
    if (!requireStudent()) {
        return;
    }

    const name = getValue("studentFullName");
    const phone = getValue("studentPhone");
    const college = getValue("studentCollege");
    const department = getValue("studentDepartment");
    const cgpa = getValue("studentCgpaInput");
    const skillsRaw = getValue("studentSkillsInput");
    const github = getValue("studentGithub");
    const linkedin = getValue("studentLinkedin");
    const resume = getValue("studentResume");
    const bio = getValue("studentBio");

    if (
        name === "" ||
        phone === "" ||
        college === "" ||
        department === "" ||
        cgpa === "" ||
        skillsRaw === ""
    ) {
        showToast("Please fill required student profile details", "warning");
        return;
    }

    const profileData = {
        name: name,
        email: currentStudent.email,
        phone: phone,
        college: college,
        department: department,
        cgpa: safeNumber(cgpa),
        skills: normalizeSkills(skillsRaw),
        github: github,
        linkedin: linkedin,
        resume: resume,
        bio: bio
    };

    try {
        const result = await apiPost("/student/profile", profileData);

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Student profile route missing in backend", "error");
            console.log("Student profile save response:", result);
            return;
        }

        currentStudent = {
            ...currentStudent,
            ...profileData
        };

        loadStudentProfile();

        hideElement("profileFormCard");

        showToast(result.data.message || "Student profile saved successfully", "success");

        await fetchJobs();
        await fetchRecommendations();
        await fetchSkillGap();

        showStudentSection("studentHome");

    } catch (error) {
        console.log(error);
        showToast("Backend connection failed while saving profile", "error");
    }
}

/* ========================================================= */
/* SAVE COMPANY PROFILE */
/* ========================================================= */

async function saveCompanyProfile() {
    if (!requireCompany()) {
        return;
    }

    const name = getValue("companyNameInput");
    const website = getValue("companyWebsite");
    const location = getValue("companyLocation");
    const hrName = getValue("companyHR");
    const contact = getValue("companyContact");
    const industry = getValue("companyIndustry");
    const employees = getValue("companyEmployees");
    const description = getValue("companyDescription");

    if (
        name === "" ||
        website === "" ||
        location === "" ||
        hrName === "" ||
        contact === "" ||
        industry === ""
    ) {
        showToast("Please fill required company profile details", "warning");
        return;
    }

    const profileData = {
        name: name,
        email: currentCompany.email,
        website: website,
        location: location,
        hrName: hrName,
        contact: contact,
        industry: industry,
        employees: safeNumber(employees, 0),
        description: description
    };

    try {
        const result = await apiPost("/company/profile", profileData);

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Company profile route missing in backend", "error");
            console.log("Company profile save response:", result);
            return;
        }

        currentCompany = {
            ...currentCompany,
            ...profileData
        };

        hideElement("companyProfileCard");

        showToast(result.data.message || "Company profile saved successfully", "success");

        await fetchCompanyJobs();

        showCompanySection("companyHome");

    } catch (error) {
        console.log(error);
        showToast("Backend connection failed while saving company profile", "error");
    }
}

/* ========================================================= */
/* JOB HELPERS */
/* ========================================================= */

function getJobId(job) {
    return job.id || job.jobId || "";
}

function getJobCompany(job) {
    return job.company || job.companyName || "N/A";
}

function getJobMatch(job) {
    return safeNumber(job.match || job.matchScore || 0);
}

function getJobEligibility(job) {
    return safeNumber(job.eligibility || 0);
}

/* ========================================================= */
/* FETCH JOBS */
/* ========================================================= */

async function fetchJobs() {
    if (!requireStudent()) {
        return;
    }

    try {
        const result = await apiGet("/jobs");

        if (!result.ok || !Array.isArray(result.data)) {
            originalJobs = [];
            displayedJobs = [];
            renderJobs([]);
            setText("totalJobs", "0");
            return;
        }

        originalJobs = result.data;
        displayedJobs = [...originalJobs];

        renderJobs(displayedJobs);

        setText("totalJobs", originalJobs.length);

    } catch (error) {
        console.log(error);
        originalJobs = [];
        displayedJobs = [];
        renderJobs([]);
        setText("totalJobs", "0");
    }
}

/* ========================================================= */
/* RENDER JOBS */
/* ========================================================= */

function renderJobs(jobList) {
    const jobsGrid = document.getElementById("jobsGrid");

    if (!jobsGrid) {
        return;
    }

    jobsGrid.innerHTML = "";

    if (!jobList || jobList.length === 0) {
        jobsGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No jobs found</h2>
                <p>Jobs will appear here when companies add them.</p>
            </div>
        `;
        return;
    }

    jobList.forEach(job => {
        const jobId = getJobId(job);
        const match = getJobMatch(job);
        const eligibility = getJobEligibility(job);

        jobsGrid.innerHTML += `
            <div class="jobCard">
                <h2>${job.title || "Untitled Job"}</h2>

                <p><b>Company:</b> ${getJobCompany(job)}</p>
                <p><b>Type:</b> ${job.type || "N/A"}</p>
                <p><b>Required CGPA:</b> ${job.cgpa || 0}</p>
                <p><b>Skills:</b> ${skillsToText(job.skills)}</p>


                <button type="button" onclick="applyJob('${jobId}')">
                    Apply Now
                </button>
            </div>
        `;
    });
}
function sortJobs() {
    applyCurrentSort();
}

function applyCurrentSort() {
    const sortType = getValue("sortJobs");

    const activeRecommendations =
        document
            .getElementById("studentRecommendations")
            .classList
            .contains("activeStudentSection");

    if (activeRecommendations) {
        let sortedRecommendations = [...recommendations];

        if (sortType === "cgpa") {
            sortedRecommendations.sort((a, b) => safeNumber(a.cgpa) - safeNumber(b.cgpa));
        }

        else if (sortType === "company") {
            sortedRecommendations.sort((a, b) => {
                return getJobCompany(a).localeCompare(getJobCompany(b));
            });
        }

        else if (sortType === "match") {
            sortedRecommendations.sort((a, b) => getJobMatch(b) - getJobMatch(a));
        }

        renderRecommendationList(sortedRecommendations);
        return;
    }

    if (sortType === "cgpa") {
        displayedJobs.sort((a, b) => safeNumber(a.cgpa) - safeNumber(b.cgpa));
    }

    else if (sortType === "company") {
        displayedJobs.sort((a, b) => {
            return getJobCompany(a).localeCompare(getJobCompany(b));
        });
    }

    else if (sortType === "match") {
        displayedJobs.sort((a, b) => getJobMatch(b) - getJobMatch(a));
    }

    renderJobs(displayedJobs);
}
/* ========================================================= */
/* SEARCH JOBS */
/* ========================================================= */

function searchJobs() {
    const search = getValue("jobSearch").toLowerCase();

    const activeRecommendations =
        document
            .getElementById("studentRecommendations")
            .classList
            .contains("activeStudentSection");

    if (activeRecommendations) {
        const filteredRecommendations = recommendations.filter(job => {
            const title = String(job.title || "").toLowerCase();
            const company = String(getJobCompany(job)).toLowerCase();
            const description = String(job.description || "").toLowerCase();

            return (
                title.includes(search) ||
                company.includes(search) ||
                description.includes(search)
            );
        });

        renderRecommendationList(filteredRecommendations);
        return;
    }

    displayedJobs = originalJobs.filter(job => {
        const title = String(job.title || "").toLowerCase();
        const company = String(getJobCompany(job)).toLowerCase();
        const skills = skillsToText(job.skills).toLowerCase();

        return (
            title.includes(search) ||
            company.includes(search) ||
            skills.includes(search)
        );
    });

    applyCurrentSort();
}
/* ========================================================= */
/* APPLY JOB */
/* ========================================================= */

async function applyJob(jobId) {
    if (!requireStudent()) {
        return;
    }

    let selectedJob = originalJobs.find(job => String(getJobId(job)) === String(jobId));

    if (!selectedJob) {
        selectedJob = recommendations.find(job => String(getJobId(job)) === String(jobId));
    }

    if (!selectedJob) {
        showToast("Job not found", "error");
        return;
    }

    const match = getJobMatch(selectedJob);

    try {
        const result = await apiPost("/apply", {
            studentEmail: currentStudent.email,
            jobId: jobId,
            company: getJobCompany(selectedJob),
            role: selectedJob.title || "N/A",
            match: match
        });

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Application failed", "error");
            return;
        }

        showToast(result.data.message || "Applied successfully", "success");

        await fetchApplications();

    } catch (error) {
        console.log(error);
        showToast("Application failed", "error");
    }
}

/* ========================================================= */
/* FETCH RECOMMENDATIONS */
/* ========================================================= */

async function fetchRecommendations() {
    if (!requireStudent()) {
        return;
    }

    try {
        const result = await apiGet("/recommendations/" + encodeURIComponent(currentStudent.email));

        if (!result.ok || !Array.isArray(result.data)) {
            recommendations = [];
            renderRecommendations();
            setText("recommendationScore", "0%");
            return;
        }

        recommendations = result.data;

        renderRecommendations();

        if (recommendations.length > 0) {
            setText("recommendationScore", getJobMatch(recommendations[0]) + "%");
        } else {
            setText("recommendationScore", "0%");
        }

    } catch (error) {
        console.log(error);
        recommendations = [];
        renderRecommendations();
        setText("recommendationScore", "0%");
    }
}

/* ========================================================= */
/* RENDER RECOMMENDATIONS */
/* ========================================================= */

function renderRecommendations() {
    const recommendationGrid = document.getElementById("recommendationGrid");

    if (!recommendationGrid) {
        return;
    }

    recommendationGrid.innerHTML = "";

    if (!recommendations || recommendations.length === 0) {
        recommendationGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No recommendations yet</h2>
                <p>Complete your profile and add skills to get job recommendations.</p>
            </div>
        `;
        return;
    }

    recommendations.forEach(job => {
        const jobId = getJobId(job);
        const match = getJobMatch(job);
        const eligibility = getJobEligibility(job);

        recommendationGrid.innerHTML += `
            <div class="recommendationCard">
                <h2>${job.title || "Untitled Job"}</h2>

                <p><b>Company:</b> ${getJobCompany(job)}</p>

                <p><b>Match Score:</b> ${match}%</p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${match}%"></div>
                </div>

                <p><b>Eligibility:</b> ${eligibility}%</p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${eligibility}%"></div>
                </div>

                <p>${job.description || ""}</p>

                <button type="button" onclick="applyJob('${jobId}')">
                    Apply
                </button>
            </div>
        `;
    });
}

function renderRecommendationList(list) {
    const recommendationGrid = document.getElementById("recommendationGrid");

    if (!recommendationGrid) {
        return;
    }

    recommendationGrid.innerHTML = "";

    if (!list || list.length === 0) {
        recommendationGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No recommendations found</h2>
                <p>Try another search or sorting option.</p>
            </div>
        `;
        return;
    }

    list.forEach(job => {
        const jobId = getJobId(job);
        const match = getJobMatch(job);
        const eligibility = getJobEligibility(job);

        recommendationGrid.innerHTML += `
            <div class="recommendationCard">
                <h2>${job.title || "Untitled Job"}</h2>

                <p><b>Company:</b> ${getJobCompany(job)}</p>

                <p><b>Match Score:</b> ${match}%</p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${match}%"></div>
                </div>

                <p><b>Eligibility:</b> ${eligibility}%</p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${eligibility}%"></div>
                </div>

                <p>${job.description || ""}</p>

                <button type="button" onclick="applyJob('${jobId}')">
                    Apply
                </button>
            </div>
        `;
    });
}
/* ========================================================= */
/* FETCH APPLICATIONS */
/* ========================================================= */

async function fetchApplications() {
    if (!requireStudent()) {
        return;
    }

    try {
        const result = await apiGet("/applications/" + encodeURIComponent(currentStudent.email));

        if (!result.ok || !Array.isArray(result.data)) {
            applications = [];
            renderApplications();
            setText("totalApplications", "0");
            return;
        }

        applications = result.data;

        renderApplications();

        setText("totalApplications", applications.length);

    } catch (error) {
        console.log(error);
        applications = [];
        renderApplications();
        setText("totalApplications", "0");
    }
}

/* ========================================================= */
/* RENDER APPLICATIONS */
/* ========================================================= */

function renderApplications() {
    const table = document.getElementById("applicationTable");

    if (!table) {
        return;
    }

    table.innerHTML = "";

    if (!applications || applications.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="5">No applications found</td>
            </tr>
        `;
        return;
    }

    applications.forEach(app => {
        const status = String(app.status || "Applied");

        table.innerHTML += `
            <tr>
                <td>${app.company || "N/A"}</td>
                <td>${app.role || "N/A"}</td>
                <td>
                    <span class="status ${status.toLowerCase()}">
                        ${status}
                    </span>
                </td>
                <td>${safeNumber(app.match || app.matchScore)}%</td>
                <td>${app.date || app.appliedDate || "N/A"}</td>
            </tr>
        `;
    });
}

/* ========================================================= */
/* FETCH SKILL GAP */
/* ========================================================= */

async function fetchSkillGap() {
    if (!requireStudent()) {
        return;
    }

    try {
        const result = await apiGet("/skillgap/" + encodeURIComponent(currentStudent.email));

        if (!result.ok || !Array.isArray(result.data)) {
            skillGapData = [];
            renderSkillGap();
            setText("eligibilityScore", "0%");
            return;
        }

        skillGapData = result.data;

        renderSkillGap();

        if (skillGapData.length > 0) {
            setText("eligibilityScore", safeNumber(skillGapData[0].score) + "%");
        } else {
            setText("eligibilityScore", "0%");
        }

    } catch (error) {
        console.log(error);
        skillGapData = [];
        renderSkillGap();
        setText("eligibilityScore", "0%");
    }
}

/* ========================================================= */
/* RENDER SKILL GAP */
/* ========================================================= */

function renderSkillGap() {
    const skillGapGrid = document.getElementById("skillGapGrid");

    if (!skillGapGrid) {
        return;
    }

    skillGapGrid.innerHTML = "";

    if (!skillGapData || skillGapData.length === 0) {
        skillGapGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No skill gap data</h2>
                <p>Skill gap analysis will appear after profile completion.</p>
            </div>
        `;
        return;
    }

    skillGapData.forEach(item => {
        const missingSkills = normalizeSkills(item.missingSkills);
        const score = safeNumber(item.score);

        skillGapGrid.innerHTML += `
            <div class="skillGapCard">
                <h2>${item.company || "N/A"}</h2>

                <p><b>Missing Skills:</b></p>

                <div class="skillsContainer">
                    ${
                        missingSkills.length > 0
                            ? missingSkills.map(skill => `<span>${skill}</span>`).join("")
                            : "<span>No missing skills</span>"
                    }
                </div>

                <p style="margin-top:20px;">
                    <b>Improvement Score:</b> ${score}%
                </p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${score}%"></div>
                </div>
            </div>
        `;
    });
}

/* ========================================================= */
/* FETCH COMPANY JOBS */
/* ========================================================= */

async function fetchCompanyJobs() {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiGet("/company/jobs/" + encodeURIComponent(currentCompany.email));

        if (!result.ok || !Array.isArray(result.data)) {
            companyJobs = [];
            renderCompanyJobs([]);
            setText("companyTotalJobs", "0");
            return;
        }

        companyJobs = result.data;

        renderCompanyJobs(companyJobs);

        setText("companyTotalJobs", companyJobs.length);

    } catch (error) {
        console.log(error);
        companyJobs = [];
        renderCompanyJobs([]);
        setText("companyTotalJobs", "0");
    }
}

/* ========================================================= */
/* RENDER COMPANY JOBS */
/* ========================================================= */

function renderCompanyJobs(jobList) {
    const manageJobsGrid = document.getElementById("manageJobsGrid");

    if (!manageJobsGrid) {
        return;
    }

    manageJobsGrid.innerHTML = "";

    if (!jobList || jobList.length === 0) {
        manageJobsGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No jobs added</h2>
                <p>Add jobs from the Add Job section.</p>
            </div>
        `;
        return;
    }

    jobList.forEach(job => {
        const jobId = getJobId(job);

        manageJobsGrid.innerHTML += `
            <div class="manageJobCard">
                <h2>${job.title || "Untitled Job"}</h2>

                <p><b>Type:</b> ${job.type || "N/A"}</p>
                <p><b>Required CGPA:</b> ${job.cgpa || 0}</p>
                <p><b>Slots:</b> ${job.slots || 0}</p>
                <p><b>Skills:</b> ${skillsToText(job.skills)}</p>
                <p>${job.description || ""}</p>

                <div class="manageButtons">
                    <button type="button" onclick="editJob('${jobId}')">
                        Edit
                    </button>

                    <button type="button" class="deleteBtn" onclick="deleteJob('${jobId}')">
                        Delete
                    </button>
                </div>
            </div>
        `;
    });
}

/* ========================================================= */
/* ADD JOB */
/* ========================================================= */

async function addJob() {
    if (!requireCompany()) {
        return;
    }

    const title = getValue("jobTitle");
    const type = getValue("jobType");
    const cgpa = getValue("jobCgpa");
    const slots = getValue("jobSlots");
    const skillsRaw = getValue("jobSkills");
    const description = getValue("jobDescription");

    if (
        title === "" ||
        type === "" ||
        cgpa === "" ||
        slots === "" ||
        skillsRaw === "" ||
        description === ""
    ) {
        showToast("Please fill all job details", "warning");
        return;
    }

    const jobData = {
        companyName: currentCompany.name,
        companyEmail: currentCompany.email,
        title: title,
        type: type,
        cgpa: safeNumber(cgpa),
        slots: parseInt(slots) || 0,
        skills: normalizeSkills(skillsRaw),
        description: description
    };

    try {
        const result = await apiPost("/jobs/add", jobData);

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Failed to add job", "error");
            return;
        }

        showToast(result.data.message || "Job added successfully", "success");

        resetForm("jobForm");

        await fetchCompanyJobs();

        showCompanySection("companyJobs");

    } catch (error) {
        console.log(error);
        showToast("Failed to add job", "error");
    }
}

/* ========================================================= */
/* DELETE JOB */
/* ========================================================= */

async function deleteJob(jobId) {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiDelete("/jobs/delete/" + encodeURIComponent(jobId));

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Delete route missing in backend", "error");
            return;
        }

        showToast(result.data.message || "Job deleted successfully", "success");

        await fetchCompanyJobs();

    } catch (error) {
        console.log(error);
        showToast("Failed to delete job", "error");
    }
}

/* ========================================================= */
/* EDIT JOB PLACEHOLDER */
/* ========================================================= */

function editJob(jobId) {
    showToast("Edit feature can be added after backend update", "warning");
    console.log("Edit job:", jobId);
}

function getStudentMatch(student) {
    if (student.match !== undefined) {
        return safeNumber(student.match);
    }

    if (student.matchScore !== undefined) {
        return safeNumber(student.matchScore);
    }

    if (
        student.applications &&
        student.applications.length > 0 &&
        student.applications[0].match !== undefined
    ) {
        return safeNumber(student.applications[0].match);
    }

    return 0;
}
/* ========================================================= */
/* FETCH APPLICANTS */
/* ========================================================= */

async function fetchApplicants() {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiGet("/company/applicants/" + encodeURIComponent(currentCompany.email));

        if (!result.ok || !Array.isArray(result.data)) {
            companyApplicants = [];
            renderApplicants([]);
            renderRecentApplicants([]);
            setText("companyApplicantsCount", "0");
            return;
        }

        companyApplicants = result.data;

        renderApplicants(companyApplicants);
        renderRecentApplicants(companyApplicants);

        setText("companyApplicantsCount", companyApplicants.length);

    } catch (error) {
        console.log(error);
        companyApplicants = [];
        renderApplicants([]);
        renderRecentApplicants([]);
        setText("companyApplicantsCount", "0");
    }
}

/* ========================================================= */
/* RENDER APPLICANTS */
/* ========================================================= */

function renderApplicants(applicants) {
    const applicantsGrid = document.getElementById("applicantsGrid");

    if (!applicantsGrid) {
        return;
    }

    applicantsGrid.innerHTML = "";

    if (!applicants || applicants.length === 0) {
        applicantsGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No applicants yet</h2>
                <p>Applicants will appear here after students apply.</p>
            </div>
        `;
        return;
    }

    applicants.forEach(student => {
        const match = getStudentMatch(student);

        applicantsGrid.innerHTML += `
            <div class="applicantCard">
                <h2>${student.name || "Student"}</h2>

                <p><b>Email:</b> ${student.email || "N/A"}</p>
                <p><b>CGPA:</b> ${student.cgpa || 0}</p>
                <p><b>Skills:</b> ${skillsToText(student.skills)}</p>
                <p><b>Match:</b> ${match}%</p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${match}%"></div>
                </div>

                <div class="applicantButtons">
                    <button type="button" onclick="shortlistStudent('${student.email}')">
                        Shortlist
                    </button>

                    <button type="button" class="deleteBtn" onclick="rejectStudent('${student.email}')">
                        Reject
                    </button>
                </div>
            </div>
        `;
    });
}

/* ========================================================= */
/* RECENT APPLICANTS */
/* ========================================================= */

function renderRecentApplicants(applicants) {
    const recentApplicants = document.getElementById("recentApplicants");

    if (!recentApplicants) {
        return;
    }

    recentApplicants.innerHTML = "";

    if (!applicants || applicants.length === 0) {
        recentApplicants.innerHTML = `<p>No recent applicants</p>`;
        return;
    }

    applicants.slice(0, 5).forEach(student => {
        recentApplicants.innerHTML += `
            <div class="recentApplicantCard">
                <h3>${student.name || "Student"}</h3>
                <p>${skillsToText(student.skills)}</p>
            </div>
        `;
    });
}

/* ========================================================= */
/* SHORTLIST STUDENT */
/* ========================================================= */

async function shortlistStudent(email) {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiPost("/shortlist", {
            companyEmail: currentCompany.email,
            studentEmail: email
        });

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Shortlist route missing in backend", "error");
            return;
        }

        showToast(result.data.message || "Student shortlisted", "success");

        await fetchApplicants();
        await fetchShortlisted();

    } catch (error) {
        console.log(error);
        showToast("Failed to shortlist student", "error");
    }
}

/* ========================================================= */
/* REJECT STUDENT */
/* ========================================================= */

async function rejectStudent(email) {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiPost("/reject", {
            companyEmail: currentCompany.email,
            studentEmail: email
        });

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Reject route missing in backend", "error");
            return;
        }

        showToast(result.data.message || "Student rejected", "success");

        await fetchApplicants();

    } catch (error) {
        console.log(error);
        showToast("Failed to reject student", "error");
    }
}

/* ========================================================= */
/* FETCH SHORTLISTED */
/* ========================================================= */

async function fetchShortlisted() {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiGet("/company/shortlisted/" + encodeURIComponent(currentCompany.email));

        if (!result.ok || !Array.isArray(result.data)) {
            shortlistedStudents = [];
            renderShortlisted([]);
            setText("companyShortlistedCount", "0");
            return;
        }

        shortlistedStudents = result.data;

        renderShortlisted(shortlistedStudents);

        setText("companyShortlistedCount", shortlistedStudents.length);

    } catch (error) {
        console.log(error);
        shortlistedStudents = [];
        renderShortlisted([]);
        setText("companyShortlistedCount", "0");
    }
}

/* ========================================================= */
/* RENDER SHORTLISTED */
/* ========================================================= */

function renderShortlisted(shortlisted) {
    const shortlistedGrid = document.getElementById("shortlistedGrid");

    if (!shortlistedGrid) {
        return;
    }

    shortlistedGrid.innerHTML = "";

    if (!shortlisted || shortlisted.length === 0) {
        shortlistedGrid.innerHTML = `
            <div class="emptyCard">
                <h2>No shortlisted students</h2>
                <p>Shortlisted students will appear here.</p>
            </div>
        `;
        return;
    }

    shortlisted.forEach(student => {
        const match = getStudentMatch(student);

        shortlistedGrid.innerHTML += `
            <div class="shortlistedCard">
                <h2>${student.name || "Student"}</h2>

                <p><b>Email:</b> ${student.email || "N/A"}</p>
                <p><b>CGPA:</b> ${student.cgpa || 0}</p>
                <p><b>Skills:</b> ${skillsToText(student.skills)}</p>
                <p><b>Recommendation Score:</b> ${match}%</p>

                <div class="progressBar">
                    <div class="progressFill" style="width:${match}%"></div>
                </div>

                <button type="button" onclick="selectStudent('${student.email}')">
                    Select Student
                </button>
            </div>
        `;
    });
}

/* ========================================================= */
/* SELECT STUDENT */
/* ========================================================= */

async function selectStudent(email) {
    if (!requireCompany()) {
        return;
    }

    try {
        const result = await apiPost("/select", {
            companyEmail: currentCompany.email,
            studentEmail: email
        });

        if (!result.ok || result.data.success === false) {
            showToast(result.data.message || "Select route missing in backend", "error");
            return;
        }

        showToast(result.data.message || "Student selected", "success");

        await fetchShortlisted();

        const selectedCount = safeNumber(
            document.getElementById("companySelectedCount")?.innerText,
            0
        );

        setText("companySelectedCount", selectedCount + 1);

    } catch (error) {
        console.log(error);
        showToast("Failed to select student", "error");
    }
}

/* ========================================================= */
/* LOGOUT */
/* ========================================================= */

function logout() {
    currentStudent = null;
    currentCompany = null;

    originalJobs = [];
    displayedJobs = [];
    recommendations = [];
    applications = [];
    skillGapData = [];
    companyJobs = [];
    companyApplicants = [];
    shortlistedStudents = [];

    showElement("profileFormCard");
    showElement("companyProfileCard");

    showToast("Logged out successfully", "success");

    goHome();
}

/* ========================================================= */
/* INITIAL LOAD */
/* ========================================================= */

window.onload = function () {
    goHome();

    console.log("script.js loaded successfully");
};