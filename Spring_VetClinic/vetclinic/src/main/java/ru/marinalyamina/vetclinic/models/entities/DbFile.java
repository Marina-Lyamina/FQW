package ru.marinalyamina.vetclinic.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "files")
public class DbFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime date;

    @Transient
    private String content;

    @ManyToMany(mappedBy = "files")
    @JsonBackReference
    private List<Appointment> appointments;


    public void initContent() throws IOException {
        content = FileManager.getFile(name);
    }
}
