package com.ags.controlekm.database.local.repositories

import com.ags.controlekm.database.local.daos.CompanyDao
import com.ags.controlekm.database.models.Company
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor(
    private val companyDao: CompanyDao
) {
    val allCompany: Flow<List<Company>> = companyDao.getAllEmpresaCliente()
    suspend fun insert(company: Company) {
        companyDao.insert(company)
    }
    suspend fun update(company: Company) {
        companyDao.update(company)
    }
    suspend fun delete(company: Company) {
        companyDao.delete(company)
    }
}