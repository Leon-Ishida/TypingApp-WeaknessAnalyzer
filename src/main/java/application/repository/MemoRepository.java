package application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import application.entity.Memo;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    
}
