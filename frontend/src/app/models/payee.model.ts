export interface PayeeDto {
  id: number;
  name: string;
  iban: string;
  bank: string;
  score: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PayeeListResponse {
  smartFavourites: PayeeDto[];
  all: PageResponse<PayeeDto>;
}

export interface PayeePayload {
  name: string;
  iban: string;
}
