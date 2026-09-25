import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/dashboard' },

  { path: '/dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/properties', name: 'properties', component: () => import('../views/Properties.vue'), meta: { requiresAuth: true } },

  { path: '/login', name: 'login', component: () => import('../views/Auth/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Auth/Register.vue') },

  // ---------- Tenant ----------
  { path: '/tenant/dashboard', name: 'tenant-dashboard', component: () => import('../views/Tenant/Dashboard.vue'), meta: { requiresAuth: true, roles: ['TENANT'] } },
  { path: '/tenant/properties', name: 'tenant-properties', component: () => import('../views/Tenant/Properties.vue'), meta: { requiresAuth: true, roles: ['TENANT'] } },
  { path: '/tenant/my-lease', name: 'tenant-lease', component: () => import('../views/Tenant/MyLease.vue'), meta: { requiresAuth: true, roles: ['TENANT'] } },
  { path: '/tenant/payments', name: 'tenant-payments', component: () => import('../views/Tenant/Payments.vue'), meta: { requiresAuth: true, roles: ['TENANT'] } },

  // ---------- Landlord ----------
  { path: '/landlord/dashboard', name: 'landlord-dashboard', component: () => import('../views/Landlord/Dashboard.vue'), meta: { requiresAuth: true, roles: ['LANDLORD'] } },
  { path: '/landlord/properties', name: 'landlord-properties', component: () => import('../views/Landlord/MyProperties.vue'), meta: { requiresAuth: true, roles: ['LANDLORD'] } },
  { path: '/landlord/applications', name: 'landlord-applications', component: () => import('../views/Landlord/Applications.vue'), meta: { requiresAuth: true, roles: ['LANDLORD'] } },
  { path: '/landlord/tenants', name: 'landlord-tenants', component: () => import('../views/Landlord/Tenants.vue'), meta: { requiresAuth: true, roles: ['LANDLORD'] } },

  // ---------- Admin ----------
  { path: '/admin/users', name: 'admin-users', component: () => import('../views/Admin/Users.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/admin/analytics', name: 'admin-analytics', component: () => import('../views/Admin/Analytics.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },

  // ---------- Fallback ----------
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  // Guest routes — bounce authenticated users to dashboard
  if (to.name === 'login' || to.name === 'register') {
    if (authStore.token) {
      next('/dashboard')
      return
    }
    next()
    return
  }

  // Requires auth
  if (to.meta.requiresAuth && !authStore.token) {
    next('/login')
    return
  }

  // Role check
  const allowedRoles = to.meta.roles as string[] | undefined
  if (allowedRoles && authStore.user?.role && !allowedRoles.includes(authStore.user.role)) {
    next('/dashboard')
    return
  }

  next()
})

export default router