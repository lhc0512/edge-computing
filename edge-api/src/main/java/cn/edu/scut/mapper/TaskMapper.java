package cn.edu.scut.mapper;

import cn.edu.scut.bean.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
