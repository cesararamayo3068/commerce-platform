/** HAL page returned by the Spring backend. */
export interface PagedModel<T> {
  _embedded?: { productResponseList?: T[] };
  page: { size: number; number: number; totalElements: number; totalPages: number };
}
