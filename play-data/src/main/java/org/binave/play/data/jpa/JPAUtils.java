package org.binave.play.data.jpa;

import org.binave.common.util.TypeUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author by bin jin on 2020/9/10 22:41.
 */
public class JPAUtils {

    /**
     * 使用默认的 META-INF/persistence.xml，其中不要使用 name="save"。
     * 可以通过 {@link LocalContainerEntityManagerFactoryBean#setPersistenceXmlLocation(String)} 进行路径设置
     */
    public static EntityManagerFactory createEntityManagerFactory(Map<String, ?> jpaProperties, String... packagesToScan) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factoryBean.setPackagesToScan(packagesToScan); // 设置扫描 Entity 的包路径
        factoryBean.setJpaPropertyMap(jpaProperties); // 设置如： "hibernate.hbm2ddl.auto", "update"
        factoryBean.afterPropertiesSet(); // 生成 EntityManagerFactory 必要的步骤
        return factoryBean.getNativeEntityManagerFactory();
    }

    /**
     * 不支持的 扫描包设置
     * @param persistenceUnitName META-INF/persistence.xml 的 name
     * @param jpaProperties     persistence.xml: property 的 kv，可以设置数据源和其他配置
     */
    @Deprecated
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map jpaProperties) {
        return Persistence.createEntityManagerFactory(persistenceUnitName, jpaProperties);
    }

    /**
     * 获得公用 EntityManager
     * 只能使用 {@link PlatformTransactionManager} 进行事务管理，
     * 无法使用 {@link javax.persistence.EntityTransaction} 进行事务管理
     */
    public static EntityManager getEntityManager(EntityManagerFactory factory) {
        return SharedEntityManagerCreator.createSharedEntityManager(factory);
    }

    /**
     * 管理事务
     */
    public static PlatformTransactionManager getPlatformTransactionManager(EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    public static PlatformTransactionManager getPlatformTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource); // spring-orm
    }

    /**
     * 用来生成 {@link TransactionStatus}
     */
    public static TransactionDefinition getTransactionDefinition(int seconds) {
        // spring-jdbc: JdbcTransactionObjectSupport
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setTimeout(seconds);
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return definition;
    }

    public static TransactionDefinition getTransactionDefinition() {
        return getTransactionDefinition(-1);
    }

    /**
     * 获得 JpaRepository
     */
    public static <T extends JpaRepository> T getJpaRepository(EntityManager manager, Class<T> type) {
        return new JpaRepositoryFactory(manager).getRepository(type);
    }

    /**
     * 初始化静态 jpa 操作类
     *
     * 未采用写时复制
     */
    public static synchronized void initJpaEntity(DBConf dbConf, Class<?> daoClass) {
        EntityManagerFactory factory = null;
        EntityManager manager = null;
        String packagePath = null;

        for (Field field : daoClass.getDeclaredFields()) {
            // 属性是否是静态的，且是 JpaEntityManager 类型或子类
            if (Modifier.isStatic(field.getModifiers()) && JpaEntityManager.class.isAssignableFrom(field.getType())) {
                // 获得属性的第一个泛型类型
                Class<?> genericType = (Class<?>) TypeUtil.getGenericTypes(field.getGenericType())[0];
                // 如果泛型是接口，并且是 JpaRepository 的子类
                if (genericType.isInterface() && JpaRepository.class.isAssignableFrom(genericType)) {
                    // 转换成子类形式
                    Class<? extends JpaRepository> jpaGenericType = (Class<? extends JpaRepository>) genericType;
                    // 获得这个接口类的第一个泛型，用来扫描包路径
                    Class<?> genericTypeGenericType = ((Class<?>) TypeUtil.getGenericTypes(
                            (jpaGenericType).getGenericInterfaces()[0]
                    )[0]);
                    if (packagePath == null) {
                        packagePath = genericTypeGenericType.getPackage().getName(); // 扫描的包名
                        factory = createEntityManagerFactory(dbConf.convertJPAProperties(), packagePath);
                        manager = getEntityManager(factory);
                    }

                    field.setAccessible(true);
                    try {
                        // 给静态属性赋值。
                        field.set(null, new JpaEntityManager<>(
                                genericTypeGenericType.getSimpleName(),
                                dbConf.getVersion(),
                                dbConf.getJdbcUrl(),
                                getPlatformTransactionManager(factory),
                                getJpaRepository(manager, jpaGenericType)
                        ));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
