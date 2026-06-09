import { Pipe, PipeTransform } from '@angular/core';
import { ProjectRequest } from '../../core/models';

@Pipe({ name: 'pendingCount', standalone: true })
export class PendingCountPipe implements PipeTransform {
  transform(requests: ProjectRequest[]): number {
    return requests.filter(r => r.status === 'PENDING').length;
  }
}

@Pipe({ name: 'acceptedCount', standalone: true })
export class AcceptedCountPipe implements PipeTransform {
  transform(requests: ProjectRequest[]): number {
    return requests.filter(r => r.status === 'ACCEPTED').length;
  }
}
