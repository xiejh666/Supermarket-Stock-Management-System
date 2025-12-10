create table category
(
    id            bigint auto_increment comment '分类ID'
        primary key,
    category_name varchar(50)                        not null comment '分类名称',
    parent_id     bigint   default 0                 null comment '父分类ID',
    sort_order    int      default 0                 null comment '排序',
    description   varchar(255)                       null comment '描述',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '商品分类表' charset = utf8mb4;

create index idx_parent_id
    on category (parent_id);

create table customer
(
    id            bigint auto_increment comment '客户ID'
        primary key,
    customer_name varchar(50)                        not null comment '客户姓名',
    phone         varchar(20)                        null comment '联系电话',
    address       varchar(255)                       null comment '地址',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '客户表' charset = utf8mb4;

create table message_notification
(
    id          bigint auto_increment comment '消息ID'
        primary key,
    user_id     bigint                               null comment '接收用户ID（NULL表示所有人可见）',
    role_code   varchar(50)                          null comment '接收角色编码（NULL表示所有角色可见）',
    type        varchar(20)                          not null comment '消息类型：success/warning/info/error',
    category    varchar(50)                          not null comment '消息分类：system/purchase/sale/inventory/user',
    title       varchar(200)                         not null comment '消息标题',
    content     text                                 not null comment '消息内容',
    link_type   varchar(50)                          null comment '跳转类型：purchase/sale/inventory/product/supplier',
    link_id     bigint                               null comment '关联业务ID',
    is_read     tinyint(1) default 0                 not null comment '是否已读：0-未读，1-已读',
    read_time   datetime                             null comment '阅读时间',
    create_time datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '消息通知表' charset = utf8mb4;

create index idx_create_time
    on message_notification (create_time);

create index idx_is_read
    on message_notification (is_read);

create index idx_role_code
    on message_notification (role_code);

create index idx_user_id
    on message_notification (user_id);

create table supplier
(
    id             bigint auto_increment comment '供应商ID'
        primary key,
    supplier_name  varchar(100)                       not null comment '供应商名称',
    contact_person varchar(50)                        null comment '联系人',
    phone          varchar(20)                        null comment '联系电话',
    address        varchar(255)                       null comment '地址',
    status         tinyint  default 1                 null comment '状态：0-禁用，1-启用',
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '供应商表' charset = utf8mb4;

create table product
(
    id            bigint auto_increment comment '商品ID'
        primary key,
    product_code  varchar(50)                           not null comment '商品编码',
    product_name  varchar(100)                          not null comment '商品名称',
    category_id   bigint                                null comment '分类ID',
    supplier_id   bigint                                null comment '供应商ID',
    unit          varchar(20) default '件'              null comment '单位',
    specification varchar(50)                           null comment '规格',
    price         decimal(10, 2)                        not null comment '售价',
    cost_price    decimal(10, 2)                        null comment '成本价',
    image         varchar(255)                          null comment '商品图片',
    description   varchar(500)                          null comment '商品描述',
    status        tinyint     default 1                 null comment '状态：0-下架，1-上架',
    create_time   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_product_code
        unique (product_code),
    constraint fk_product_category
        foreign key (category_id) references category (id),
    constraint fk_product_supplier
        foreign key (supplier_id) references supplier (id)
)
    comment '商品表' charset = utf8mb4;

create table inventory
(
    id               bigint auto_increment comment 'ID'
        primary key,
    product_id       bigint                             not null comment '商品ID',
    quantity         int      default 0                 null comment '库存数量',
    warning_quantity int      default 10                null comment '预警数量',
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_product_id
        unique (product_id),
    constraint fk_inventory_product
        foreign key (product_id) references product (id)
            on delete cascade
)
    comment '库存表' charset = utf8mb4;

create index idx_quantity
    on inventory (quantity);

create index idx_category_id
    on product (category_id);

create index idx_supplier_id
    on product (supplier_id);

create table sys_permission
(
    id              bigint auto_increment comment '权限ID'
        primary key,
    permission_name varchar(50)                        not null comment '权限名称',
    permission_code varchar(100)                       not null comment '权限编码',
    parent_id       bigint   default 0                 null comment '父权限ID',
    permission_type tinyint  default 1                 null comment '权限类型：1-菜单，2-按钮',
    path            varchar(255)                       null comment '路由路径',
    icon            varchar(50)                        null comment '图标',
    sort_order      int      default 0                 null comment '排序',
    create_time     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_permission_code
        unique (permission_code)
)
    comment '权限表' charset = utf8mb4;

create table sys_role
(
    id          bigint auto_increment comment '角色ID'
        primary key,
    role_name   varchar(50)                        not null comment '角色名称',
    role_code   varchar(50)                        not null comment '角色编码',
    description varchar(255)                       null comment '角色描述',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_role_code
        unique (role_code)
)
    comment '角色表' charset = utf8mb4;

create table sys_role_permission
(
    id            bigint auto_increment comment 'ID'
        primary key,
    role_id       bigint                             not null comment '角色ID',
    permission_id bigint                             not null comment '权限ID',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_role_permission
        unique (role_id, permission_id),
    constraint fk_role_permission_permission
        foreign key (permission_id) references sys_permission (id)
            on delete cascade,
    constraint fk_role_permission_role
        foreign key (role_id) references sys_role (id)
            on delete cascade
)
    comment '角色权限关联表' charset = utf8mb4;

create index idx_permission_id
    on sys_role_permission (permission_id);

create index idx_role_id
    on sys_role_permission (role_id);

create table sys_user
(
    id          bigint auto_increment comment '用户ID'
        primary key,
    username    varchar(50)                        not null comment '用户名',
    password    varchar(255)                       not null comment '密码',
    real_name   varchar(50)                        null comment '真实姓名',
    phone       varchar(20)                        null comment '手机号',
    email       varchar(100)                       null comment '邮箱',
    avatar      varchar(500)                       null comment '用户头像URL',
    status      tinyint  default 1                 null comment '状态：0-禁用，1-启用',
    role_id     bigint                             null comment '角色ID',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_username
        unique (username),
    constraint fk_user_role
        foreign key (role_id) references sys_role (id)
)
    comment '用户表' charset = utf8mb4;

create table inventory_log
(
    id              bigint auto_increment comment 'ID'
        primary key,
    product_id      bigint                             not null comment '商品ID',
    change_type     tinyint                            not null comment '变动类型：1-入库，2-出库，3-盘点调整',
    change_quantity int                                not null comment '变动数量（正数表示增加，负数表示减少）',
    before_quantity int                                not null comment '变动前数量',
    after_quantity  int                                not null comment '变动后数量',
    order_no        varchar(50)                        null comment '关联订单号',
    remark          varchar(255)                       null comment '备注',
    operator_id     bigint                             not null comment '操作人ID',
    create_time     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint fk_inventory_log_operator
        foreign key (operator_id) references sys_user (id),
    constraint fk_inventory_log_product
        foreign key (product_id) references product (id)
)
    comment '库存变动日志表' charset = utf8mb4;

create index idx_create_time
    on inventory_log (create_time);

create index idx_operator_id
    on inventory_log (operator_id);

create index idx_product_id
    on inventory_log (product_id);

create table purchase_order
(
    id            bigint auto_increment comment '采购订单ID'
        primary key,
    order_no      varchar(50)                              not null comment '订单编号',
    supplier_id   bigint                                   not null comment '供应商ID',
    total_amount  decimal(10, 2) default 0.00              null comment '订单总金额',
    purchase_date date                                     null comment '采购日期',
    status        tinyint        default 0                 null comment '订单状态：0-待审核，1-已通过，2-已拒绝，3-已入库',
    applicant_id  bigint                                   not null comment '申请人ID',
    auditor_id    bigint                                   null comment '审核人ID',
    audit_time    datetime                                 null comment '审核时间',
    audit_remark  varchar(500)                             null comment '审核备注',
    inbound_time  datetime                                 null comment '入库时间',
    create_time   datetime       default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_order_no
        unique (order_no),
    constraint fk_purchase_applicant
        foreign key (applicant_id) references sys_user (id),
    constraint fk_purchase_auditor
        foreign key (auditor_id) references sys_user (id),
    constraint fk_purchase_supplier
        foreign key (supplier_id) references supplier (id)
)
    comment '采购订单表' charset = utf8mb4;

create index idx_applicant_id
    on purchase_order (applicant_id);

create index idx_status
    on purchase_order (status);

create index idx_supplier_id
    on purchase_order (supplier_id);

create table purchase_order_item
(
    id          bigint auto_increment comment 'ID'
        primary key,
    order_id    bigint                             not null comment '订单ID',
    product_id  bigint                             not null comment '商品ID',
    quantity    int                                not null comment '数量',
    unit_price  decimal(10, 2)                     not null comment '单价',
    total_price decimal(10, 2)                     not null comment '总价',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint fk_purchase_item_order
        foreign key (order_id) references purchase_order (id)
            on delete cascade,
    constraint fk_purchase_item_product
        foreign key (product_id) references product (id)
)
    comment '采购订单明细表' charset = utf8mb4;

create index idx_order_id
    on purchase_order_item (order_id);

create index idx_product_id
    on purchase_order_item (product_id);

create table sale_order
(
    id            bigint auto_increment comment '销售订单ID'
        primary key,
    order_no      varchar(50)                              not null comment '订单编号',
    customer_id   bigint                                   null comment '客户ID',
    total_amount  decimal(10, 2) default 0.00              null comment '订单总金额',
    status        tinyint        default 0                 null comment '订单状态：0-待支付，1-已支付，2-已取消',
    cashier_id    bigint                                   not null comment '收银员ID',
    create_time   datetime       default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    cancel_reason varchar(255)                             null comment '取消原因',
    constraint uk_order_no
        unique (order_no),
    constraint fk_sale_cashier
        foreign key (cashier_id) references sys_user (id),
    constraint fk_sale_customer
        foreign key (customer_id) references customer (id)
)
    comment '销售订单表' charset = utf8mb4;

create index idx_cashier_id
    on sale_order (cashier_id);

create index idx_customer_id
    on sale_order (customer_id);

create index idx_status
    on sale_order (status);

create table sale_order_item
(
    id          bigint auto_increment comment 'ID'
        primary key,
    order_id    bigint                             not null comment '订单ID',
    product_id  bigint                             not null comment '商品ID',
    quantity    int                                not null comment '数量',
    unit_price  decimal(10, 2)                     not null comment '单价',
    total_price decimal(10, 2)                     not null comment '总价',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint fk_sale_item_order
        foreign key (order_id) references sale_order (id)
            on delete cascade,
    constraint fk_sale_item_product
        foreign key (product_id) references product (id)
)
    comment '销售订单明细表' charset = utf8mb4;

create index idx_order_id
    on sale_order_item (order_id);

create index idx_product_id
    on sale_order_item (product_id);

create index idx_role_id
    on sys_user (role_id);

create table system_config
(
    id           bigint auto_increment comment '配置ID'
        primary key,
    config_key   varchar(100)                       not null comment '配置键',
    config_value text                               null comment '配置值',
    config_desc  varchar(200)                       null comment '配置描述',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_config_key
        unique (config_key)
)
    comment '系统配置表' charset = utf8mb4;

create table system_notification
(
    id            bigint auto_increment
        primary key,
    title         varchar(200)                       not null,
    content       text                               not null,
    type          varchar(50)                        not null,
    receiver_id   bigint                             null,
    sender_id     bigint                             not null,
    business_id   bigint                             null,
    business_type varchar(50)                        null,
    is_read       tinyint  default 0                 not null,
    priority      tinyint  default 2                 not null,
    create_time   datetime default CURRENT_TIMESTAMP not null,
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    charset = utf8mb4;

create index idx_receiver_id
    on system_notification (receiver_id);

create index idx_sender_id
    on system_notification (sender_id);

create table user_config
(
    id           bigint auto_increment comment '配置ID'
        primary key,
    user_id      bigint                             not null comment '用户ID',
    config_key   varchar(100)                       not null comment '配置键',
    config_value varchar(500)                       null comment '配置值',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_config
        unique (user_id, config_key)
)
    comment '用户配置表' charset = utf8mb4;

create index idx_user_id
    on user_config (user_id);

