package placement;

import java.util.ArrayList;
import java.util.List;

public class Algorithms {

    /* ========================= */
    /* SKILL MATCH */
    /* ========================= */

    public static int calculateSkillMatch(

            Student student,

            Job job

    ){

        if(
                job.skills == null
                ||
                job.skills.size() == 0
        ){

            return 0;

        }

        int matched = 0;

        for(String required : job.skills){

            for(String studentSkill : student.skills){

                if(
                        required.trim()
                                .equalsIgnoreCase(
                                        studentSkill.trim()
                                )
                ){

                    matched++;

                    break;

                }

            }

        }

        return
                (int)(
                        (
                                matched * 100.0
                        )
                                /
                                job.skills.size()
                );

    }

    /* ========================= */
    /* ELIGIBILITY */
    /* ========================= */

    public static int calculateEligibility(

            Student student,

            Job job

    ){

        int skillScore =
                calculateSkillMatch(
                        student,
                        job
                );

        double cgpaScore =
                (
                        student.cgpa
                                /
                                job.cgpa
                ) * 100;

        if(cgpaScore > 100){

            cgpaScore = 100;

        }

        return
                (int)(
                        (
                                skillScore
                                        +
                                        cgpaScore
                        ) / 2
                );

    }

    /* ========================= */
    /* RECOMMENDATIONS */
    /* ========================= */

    public static List<Recommendation>
    generateRecommendations(

            Student student,

            List<Job> jobs

    ){

        List<Recommendation> result =
                new ArrayList<>();

        for(Job job : jobs){

            int match =
                    calculateSkillMatch(
                            student,
                            job
                    );

            int eligibility =
                    calculateEligibility(
                            student,
                            job
                    );

            Recommendation rec =
                    new Recommendation(

                            job.id,

                            job.title,

                            job.company,

                            match,

                            eligibility,

                            job.description

                    );

            result.add(rec);

        }

        sortRecommendations(result);

        return result;

    }

    /* ========================= */
    /* SORT RECOMMENDATIONS */
    /* ========================= */

    public static void sortRecommendations(

            List<Recommendation> list

    ){

        list.sort(

                (
                        a,
                        b
                ) ->

                        Integer.compare(
                                b.match,
                                a.match
                        )

        );

    }

    /* ========================= */
    /* MERGE SORT JOBS */
    /* ========================= */

    public static void mergeSortJobs(

            List<Job> jobs,

            int left,

            int right

    ){

        if(left < right){

            int mid =
                    (left + right) / 2;

            mergeSortJobs(
                    jobs,
                    left,
                    mid
            );

            mergeSortJobs(
                    jobs,
                    mid + 1,
                    right
            );

            merge(
                    jobs,
                    left,
                    mid,
                    right
            );

        }

    }

    /* ========================= */
    /* MERGE */
    /* ========================= */

    public static void merge(

            List<Job> jobs,

            int left,

            int mid,

            int right

    ){

        int n1 =
                mid - left + 1;

        int n2 =
                right - mid;

        List<Job> leftArr =
                new ArrayList<>();

        List<Job> rightArr =
                new ArrayList<>();

        for(int i=0;i<n1;i++){

            leftArr.add(
                    jobs.get(left + i)
            );

        }

        for(int j=0;j<n2;j++){

            rightArr.add(
                    jobs.get(mid + 1 + j)
            );

        }

        int i=0;
        int j=0;
        int k=left;

        while(i<n1 && j<n2){

            if(
                    leftArr.get(i).cgpa
                    >=
                    rightArr.get(j).cgpa
            ){

                jobs.set(
                        k,
                        leftArr.get(i)
                );

                i++;

            }

            else{

                jobs.set(
                        k,
                        rightArr.get(j)
                );

                j++;

            }

            k++;

        }

        while(i<n1){

            jobs.set(
                    k,
                    leftArr.get(i)
            );

            i++;
            k++;

        }

        while(j<n2){

            jobs.set(
                    k,
                    rightArr.get(j)
            );

            j++;
            k++;

        }

    }

    /* ========================= */
    /* BINARY SEARCH */
    /* ========================= */

    public static Job binarySearchJob(

            List<Job> jobs,

            String title

    ){

        int left = 0;

        int right = jobs.size() - 1;

        while(left <= right){

            int mid =
                    (left + right) / 2;

            Job job =
                    jobs.get(mid);

            int compare =
                    job.title
                            .compareToIgnoreCase(
                                    title
                            );

            if(compare == 0){

                return job;

            }

            else if(compare < 0){

                left = mid + 1;

            }

            else{

                right = mid - 1;

            }

        }

        return null;

    }

    /* ========================= */
    /* SKILL GAP ANALYSIS */
    /* ========================= */

    public static List<SkillGap>
    analyzeSkillGap(

            Student student,

            List<Job> jobs

    ){

        List<SkillGap> gaps =
                new ArrayList<>();

        for(Job job : jobs){

            List<String> missing =
                    new ArrayList<>();

            for(String skill : job.skills){

                boolean found = false;

                for(String studentSkill : student.skills){

                    if(
                            skill.trim()
                                    .equalsIgnoreCase(
                                            studentSkill.trim()
                                    )
                    ){

                        found = true;

                        break;

                    }

                }

                if(!found){

                    missing.add(skill);

                }

            }

            int score =
                    calculateSkillMatch(
                            student,
                            job
                    );

            gaps.add(

                    new SkillGap(

                            job.company,

                            missing,

                            score

                    )

            );

        }

        return gaps;

    }

    /* ========================= */
    /* TOP STUDENTS */
    /* ========================= */

    public static List<Student>
    getTopStudents(

            List<Student> students

    ){

        List<Student> list =
                new ArrayList<>(students);

        list.sort(

                (
                        a,
                        b
                ) ->

                        Double.compare(
                                b.cgpa,
                                a.cgpa
                        )

        );

        return list;

}
}
