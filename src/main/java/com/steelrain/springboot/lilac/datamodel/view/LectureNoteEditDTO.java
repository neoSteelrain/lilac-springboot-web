package com.steelrain.springboot.lilac.datamodel.view;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LectureNoteEditDTO {
    private Long noteId;

    @NotBlank
    @Length(min=1, max=100)
    private String noteTitle;

    @NotBlank
    @Length(min=1, max=500)
    private String noteDescription;
    private int lectureId;
    private int subjectId;
}
