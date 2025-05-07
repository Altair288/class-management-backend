// package com.altair288.class_management.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// import com.altair288.class_management.repository.UserRepository;
// import com.altair288.class_management.model.User;

// import java.util.Collections;

// @Service
// public class MyUserDetailsService implements UserDetailsService {
//     @Autowired
//     private UserRepository userRepository;

//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         // 从数据库查找用户
//         User user = userRepository.findByUsername(username)
//                                   .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
//         if (user == null) throw new UsernameNotFoundException("用户不存在");
//         // 返回实现了UserDetails的对象
//         return new org.springframework.security.core.userdetails.User(
//             user.getUsername(),
//             user.getPassword(),
//             Collections.emptyList()
//         );
//     }
// }
