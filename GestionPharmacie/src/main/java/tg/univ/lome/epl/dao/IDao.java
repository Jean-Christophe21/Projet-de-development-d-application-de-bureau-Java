/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package tg.univ.lome.epl.dao;

import java.util.List;
import java.util.Optional;


/**
 * @author USER
 * Interface générique pour tous les DAOs.
 * T  = type de l'entité
 * ID = type de la clé primaire
 */
public interface IDao<T, ID> {

    /** Insère une entité et retourne l'id généré. */
    int insert(T entity) throws Exception;

    /** Met à jour une entité existante. */
    boolean update(T entity) throws Exception;

    /** Supprime une entité par son id. */
    boolean delete(ID id) throws Exception;

    /** Trouve une entité par son id. */
    Optional<T> findById(ID id) throws Exception;

    /** Retourne toutes les entités. */
    List<T> findAll() throws Exception;
}