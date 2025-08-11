package demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pagination data wrapper
 *
 * @param <T> the type of the current page data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean<T> {
    private Long total;      // total number of records
    private List<T> rows;    // list of data in the current page
    private Integer page;    // current page number
    private Integer pageSize; // size of each page

    public PageBean(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }
}
