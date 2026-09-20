import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/request'

const routes = [
    {
        path: '/home',
        redirect: '/'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/',
        component: () => import('../layouts/Layout.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('../views/Home.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'article/:id',
                name: 'ArticleDetail',
                component: () => import('../views/ArticleDetail.vue'),
                meta: { requiresAuth: false }  // 文章详情公开访问
            },
            {
                path: 'publish/:id?',
                name: 'ArticlePublish',
                component: () => import('../views/ArticlePublish.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'knowledge',
                name: 'Knowledge',
                component: () => import('../views/Knowledge.vue'),
                meta: { requiresAuth: true }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 登录守卫：检查 Token
router.beforeEach((to, from, next) => {
    const token = getToken()
    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router
