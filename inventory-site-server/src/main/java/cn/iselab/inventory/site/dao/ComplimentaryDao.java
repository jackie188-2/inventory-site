package cn.iselab.inventory.site.dao;

import cn.iselab.inventory.site.model.Complimentary;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

/**
 * Created by ROGK on 2017/11/1.
 */
@Transactional
public interface ComplimentaryDao extends CrudRepository<Complimentary,Long> ,JpaSpecificationExecutor<Complimentary>{
}
