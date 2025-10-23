package ch.hearc.ig.guideresto.business;

/**
 * @author cedric.baudet
 */

import ch.hearc.ig.guideresto.persistence.ConnectionUtils;
import ch.hearc.ig.guideresto.persistence.GradeMapper;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CompleteEvaluation extends Evaluation {

    private String comment;
    private String username;
    private Set<Grade> grades;

    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
        this.grades = new HashSet();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Grade> getGrades() {
        if (this.grades == null) {
            Connection connection = ConnectionUtils.getConnection();
            GradeMapper gradesMapper = new GradeMapper(connection);            this.grades = gradesMapper.findForCompleteEvaluation(this.getId());
            this.grades = gradesMapper.findForCompleteEvaluation(this.getId());
        }
        return this.grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}